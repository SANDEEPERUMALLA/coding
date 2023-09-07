package com.goverance;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class MutliLevelLimitMap {

    public static void main(String[] args) {
        List<String> clientIds = List.of("c1", "c2");
//        List<String> clientIds = List.of("c1");
        List<String> namespaces = List.of("ns1", "ns2", "ns3");
//        List<String> namespaces = List.of("ns1", "ns2", "ns3", "ns4", "ns5");
        List<String> subNamespaces = List.of("subns1", "subns2");
        List<String> tenants = List.of("ORG1", "ORG2", "ORG3");

        TopLevelLimitData topLevelLimitData = new TopLevelLimitData("", 100000);

        System.out.println("List of tenants !!!");

        clientIds.forEach(clientId -> {
            namespaces.forEach(namespace -> {
                subNamespaces.forEach(subNamespace -> {
                    tenants.forEach(tenant -> {
                        System.out.println(String.join(":", clientId, namespace, subNamespace, tenant));
                        int keyCount = 10;
                        IntStream.rangeClosed(1, keyCount).forEach(e -> {
                            String key = RandomStringUtils.randomAlphabetic(10);
                            String fullyQualifiedKey = String.join(":", clientId, namespace, subNamespace, tenant, key);
                            String[] keyParts = fullyQualifiedKey.split(":", -1);
                            String client = keyParts[0];
                            String ns = keyParts[1];
                            String subns = keyParts[2];
                            String tenantId = keyParts[3];
                            ClientLevelLimitData clientLevelLimitData = topLevelLimitData.getClientMap().computeIfAbsent(client, k -> new ClientLevelLimitData(k));
                            clientLevelLimitData.getNamespaceListForWhichLimitIsApplicable().add(ns);

                            NamespaceLevelLimitData namespaceLevelLimitData = clientLevelLimitData.getNamespaceDataMap().computeIfAbsent(ns, k -> new NamespaceLevelLimitData(ns));
                            namespaceLevelLimitData.getSubnamespaceListForWhichLimitIsApplicable().add(subns);

                            SubNamespaceLevelLimitData subNamespaceLevelLimitData = namespaceLevelLimitData.getSubNamespaceDataMap().computeIfAbsent(subns, k -> new SubNamespaceLevelLimitData(subns));
                            subNamespaceLevelLimitData.getOrgListForWhichLimitIsApplicable().add(tenantId);

                            OrgLevelLimitData orgLevelLimitData = subNamespaceLevelLimitData.getOrgDataMap().computeIfAbsent(tenantId, k -> new OrgLevelLimitData(tenantId));

                        });
                    });
                });
            });
        });

        System.out.println();
        System.out.println();
        System.out.println();

        List<Pair<String, Long>> limits = List.of(
//                Pair.of("c1", 200L),
//                Pair.of("c1:ns1", 50L),
//                Pair.of("c1:ns1:subns1", 20L),
//                Pair.of("c1:ns1:subns1:ORG1", 10L),
//                Pair.of("c1:ns2", 60L),
//                Pair.of("c1:ns2:subns2", 40L),
//                Pair.of("c1:ns2:subns2:ORG2", 10L),
//                Pair.of("c1:ns2:subns2:ORG1", 10L)
                Pair.of("c1", 100L),
                Pair.of("c1:ns2", 20L),
                Pair.of("c1:ns1", 20L)
        );

        limits.forEach(limit -> {
            Long limitValue = limit.getRight();
            String limitStr = limit.getLeft();
            String[] limitParts = limitStr.split(":", -1);
            updateLimitsData(topLevelLimitData, limitParts, limitValue);
        });

        updateLimitsDataBasedOnChildLimits(topLevelLimitData);

        applyLimits(topLevelLimitData);

    }

    private static void updateLimitsDataBasedOnChildLimits(TopLevelLimitData topLevelLimitData) {
        topLevelLimitData.getClientMap().forEach((k, v) -> {
            updateLimitsDataBasedOnChildLimits(v);
        });

    }

    private static void updateLimitsDataBasedOnChildLimits(ClientLevelLimitData clientLevelLimitData) {

        AtomicLong limitToBeReduced = new AtomicLong(0);
        clientLevelLimitData.getNamespaceDataMap().forEach((k, v) -> {
            if (!clientLevelLimitData.getNamespaceListForWhichLimitIsApplicable().contains(k)) {
                limitToBeReduced.addAndGet(clientLevelLimitData.getNamespaceDataMap().get(k).getLimit());
            }
        });
        clientLevelLimitData.setLimit(clientLevelLimitData.getLimit() - limitToBeReduced.get());
        clientLevelLimitData.getNamespaceDataMap().forEach((k, v) -> {
            updateLimitsDataBasedOnChildLimits(v);
        });
    }

    private static void updateLimitsDataBasedOnChildLimits(NamespaceLevelLimitData namespaceLevelLimitData) {

        AtomicLong limitToBeReduced = new AtomicLong(0);
        namespaceLevelLimitData.getSubNamespaceDataMap().forEach((k, v) -> {
            if (!namespaceLevelLimitData.getSubnamespaceListForWhichLimitIsApplicable().contains(k)) {
                limitToBeReduced.addAndGet(namespaceLevelLimitData.getSubNamespaceDataMap().get(k).getLimit());
            }
        });
        namespaceLevelLimitData.setLimit(namespaceLevelLimitData.getLimit() - limitToBeReduced.get());
        namespaceLevelLimitData.getSubNamespaceDataMap().forEach((k, v) -> {
            updateLimitsDataBasedOnChildLimits(v);
        });
    }

    private static void updateLimitsDataBasedOnChildLimits(SubNamespaceLevelLimitData subNamespaceLevelLimitData) {

        AtomicLong limitToBeReduced = new AtomicLong(0);
        subNamespaceLevelLimitData.getOrgDataMap().forEach((k, v) -> {
            if (!subNamespaceLevelLimitData.getOrgListForWhichLimitIsApplicable().contains(k)) {
                limitToBeReduced.addAndGet(subNamespaceLevelLimitData.getOrgDataMap().get(k).getLimit());
            }
        });
        subNamespaceLevelLimitData.setLimit(subNamespaceLevelLimitData.getLimit() - limitToBeReduced.get());
        subNamespaceLevelLimitData.getOrgDataMap().forEach((k, v) -> {
            updateLimitsData(v);
        });
    }

    private static void updateLimitsData(OrgLevelLimitData v) {

    }

    private static void applyLimits(TopLevelLimitData topLevelLimitData) {
        topLevelLimitData.getClientMap().forEach((k, v) -> {

            applyLimits(v);
        });
    }

    private static void applyLimits(ClientLevelLimitData clientLevelLimitData) {
        clientLevelLimitData.getNamespaceDataMap().forEach((k, v) -> {
            applyLimits(v, clientLevelLimitData.getClientName());
        });
        long limit = clientLevelLimitData.getLimit();
        if (limit != 0) {
            List<String> tenants = new ArrayList<>();
            clientLevelLimitData.getNamespaceDataMap().forEach((k, v) -> {
                if (!clientLevelLimitData.getNamespaceListForWhichLimitIsApplicable().contains(k)) {
                    return;
                }
                v.getSubNamespaceDataMap().forEach((k1, v1) -> {
                    if (!v.getSubnamespaceListForWhichLimitIsApplicable().contains(k1)) {
                        return;
                    }
                    v1.getOrgDataMap().forEach((k2, v2) -> {
                        if (!v1.getOrgListForWhichLimitIsApplicable().contains(k2)) {
                            return;
                        }
                        tenants.add(String.join(":", clientLevelLimitData.getClientName(), k, k1, k2));
                    });
                });
            });
            System.out.println(String.format("Tenant list for eviction for namespace \"%s\": %s, limit: %d", clientLevelLimitData.getClientName(),
                    tenants, limit));
        }

    }

    private static void applyLimits(NamespaceLevelLimitData namespaceLevelLimitData, String clientName) {
        namespaceLevelLimitData.getSubNamespaceDataMap().forEach((k, v) -> {
            applyLimits(v, clientName, namespaceLevelLimitData.getNamespace());
        });
        long limit = namespaceLevelLimitData.getLimit();
        List<String> tenants = new ArrayList<>();
        if (limit != 0) {
            namespaceLevelLimitData.getSubNamespaceDataMap().forEach((k, v) -> {
                if (!namespaceLevelLimitData.getSubnamespaceListForWhichLimitIsApplicable().contains(k)) {
                    return;
                }
                v.getOrgDataMap().forEach((k1, v1) -> {
                    if (!v.getOrgListForWhichLimitIsApplicable().contains(k1)) {
                        return;
                    }
                    tenants.add(String.join(":", clientName, namespaceLevelLimitData.getNamespace(), k, k1));
                });
            });
            System.out.println(String.format("Tenant list for eviction for subnamespace \"%s\": %s, limit: %d", String.join(":", clientName, namespaceLevelLimitData.getNamespace()),
                    tenants, limit));

        }
    }

    private static void applyLimits(SubNamespaceLevelLimitData subNamespaceLevelLimitData, String clientName, String namespace) {

        subNamespaceLevelLimitData.getOrgDataMap().forEach((k, v) -> {
            applyLimits(v, clientName, namespace, subNamespaceLevelLimitData.getSubNamespace());
        });

        List<String> tenants = new ArrayList<>();
        long limit = subNamespaceLevelLimitData.getLimit();
        if (limit != 0) {
            subNamespaceLevelLimitData.getOrgDataMap().forEach((k, v) -> {
                if (!subNamespaceLevelLimitData.getOrgListForWhichLimitIsApplicable().contains(k)) {
                    return;
                }
                tenants.add(String.join(":", clientName, namespace, subNamespaceLevelLimitData.getSubNamespace(), k));
            });
            System.out.println(String.format("Tenant list for eviction for subnamespace \"%s\": %s, limit : %d", String.join(":", clientName, namespace, subNamespaceLevelLimitData.getSubNamespace()),
                    tenants, limit));
        }

    }

    private static void applyLimits(OrgLevelLimitData orgLevelLimitData, String clientName, String namespace, String subNamespace) {

        long limit = orgLevelLimitData.getLimit();
        if (limit != 0) {
            List<String> tenants = new ArrayList<>();
            tenants.add(String.join(":", clientName, namespace, subNamespace, orgLevelLimitData.getOrgName()));
            System.out.println(String.format("Tenant list for eviction for tenant \"%s\": %s, limit : %d", String.join(":", clientName, namespace, subNamespace, orgLevelLimitData.getOrgName()),
                    tenants, limit));
        }

    }

    public static void updateLimitsData(TopLevelLimitData topLevelLimitData, String[] limitParts, Long limitValue) {

        if (limitParts.length == 1) {
            updateClientLevelData(topLevelLimitData, limitParts, limitValue);
        } else if (limitParts.length == 2) {
            updateNamespaceLevelData(topLevelLimitData, limitParts, limitValue);
        } else if (limitParts.length == 3) {
            updateSubNamespaceLevelData(topLevelLimitData, limitParts, limitValue);
        } else if (limitParts.length == 4) {
            updateTenantLevelData(topLevelLimitData, limitParts, limitValue);
        }
    }

    public static void updateClientLevelData(TopLevelLimitData topLevelLimitData, String[] keyParts, long limit) {
        String client = keyParts[0];
        ClientLevelLimitData clientLevelLimitData = topLevelLimitData.getClientMap().get(client);
        clientLevelLimitData.setLimit(limit);
        topLevelLimitData.getClientListForWhichLimitIsApplicable().remove(client);
    }

    public static void updateNamespaceLevelData(TopLevelLimitData topLevelLimitData, String[] keyParts, long limit) {
        String client = keyParts[0];
        String ns = keyParts[1];
        ClientLevelLimitData clientLevelLimitData = topLevelLimitData.getClientMap().get(client);
        clientLevelLimitData.getNamespaceListForWhichLimitIsApplicable().remove(ns);
        NamespaceLevelLimitData namespaceLevelLimitData = clientLevelLimitData.getNamespaceDataMap().get(ns);
        namespaceLevelLimitData.setLimit(limit);
    }

    public static void updateSubNamespaceLevelData(TopLevelLimitData topLevelLimitData, String[] keyParts, long limit) {
        String client = keyParts[0];
        String ns = keyParts[1];
        String subNs = keyParts[2];
        ClientLevelLimitData clientLevelLimitData = topLevelLimitData.getClientMap().get(client);
        NamespaceLevelLimitData namespaceLevelLimitData = clientLevelLimitData.getNamespaceDataMap().get(ns);
        namespaceLevelLimitData.getSubnamespaceListForWhichLimitIsApplicable().remove(subNs);
        SubNamespaceLevelLimitData subNamespaceLevelLimitData = namespaceLevelLimitData.getSubNamespaceDataMap().get(subNs);
        subNamespaceLevelLimitData.setLimit(limit);
    }

    public static void updateTenantLevelData(TopLevelLimitData topLevelLimitData, String[] keyParts, long limit) {
        String client = keyParts[0];
        String ns = keyParts[1];
        String subNs = keyParts[2];
        String tenant = keyParts[3];
        ClientLevelLimitData clientLevelLimitData = topLevelLimitData.getClientMap().get(client);
        NamespaceLevelLimitData namespaceLevelLimitData = clientLevelLimitData.getNamespaceDataMap().get(ns);
        SubNamespaceLevelLimitData subNamespaceLevelLimitData = namespaceLevelLimitData.getSubNamespaceDataMap().get(subNs);
        subNamespaceLevelLimitData.getOrgListForWhichLimitIsApplicable().remove(tenant);
        OrgLevelLimitData orgLevelLimitData = subNamespaceLevelLimitData.getOrgDataMap().get(tenant);
        orgLevelLimitData.setLimit(limit);
    }


    public static class TopLevelLimitData {
        private String nodeName = "";
        private long limit;
        private Set<String> clientListForWhichLimitIsApplicable = new HashSet<>();
        Map<String, ClientLevelLimitData> clientMap = new ConcurrentHashMap<>();

        public TopLevelLimitData(String nodeName, long limit) {
            this.nodeName = nodeName;
            this.limit = limit;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public long getLimit() {
            return limit;
        }

        public void setLimit(long limit) {
            this.limit = limit;
        }

        public Set<String> getClientListForWhichLimitIsApplicable() {
            return clientListForWhichLimitIsApplicable;
        }

        public void setClientListForWhichLimitIsApplicable(Set<String> clientListForWhichLimitIsApplicable) {
            this.clientListForWhichLimitIsApplicable = clientListForWhichLimitIsApplicable;
        }

        public Map<String, ClientLevelLimitData> getClientMap() {
            return clientMap;
        }

        public void setClientMap(Map<String, ClientLevelLimitData> clientMap) {
            this.clientMap = clientMap;
        }
    }

    public static class ClientLevelLimitData {
        private String clientName;
        private long limit;
        private Set<String> namespaceListForWhichLimitIsApplicable = new HashSet<>();
        Map<String, NamespaceLevelLimitData> namespaceDataMap = new ConcurrentHashMap<>();

        public ClientLevelLimitData(String clientName) {
            this.clientName = clientName;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }

        public long getLimit() {
            return limit;
        }

        public void setLimit(long limit) {
            this.limit = limit;
        }

        public Set<String> getNamespaceListForWhichLimitIsApplicable() {
            return namespaceListForWhichLimitIsApplicable;
        }

        public void setNamespaceListForWhichLimitIsApplicable(Set<String> namespaceListForWhichLimitIsApplicable) {
            this.namespaceListForWhichLimitIsApplicable = namespaceListForWhichLimitIsApplicable;
        }

        public Map<String, NamespaceLevelLimitData> getNamespaceDataMap() {
            return namespaceDataMap;
        }

        public void setNamespaceDataMap(Map<String, NamespaceLevelLimitData> namespaceDataMap) {
            this.namespaceDataMap = namespaceDataMap;
        }
    }

    private static class NamespaceLevelLimitData {
        private String namespace;
        private long limit;
        private Set<String> subnamespaceListForWhichLimitIsApplicable = new HashSet<>();
        Map<String, SubNamespaceLevelLimitData> subNamespaceDataMap = new ConcurrentHashMap<>();

        public NamespaceLevelLimitData(String namespace) {
            this.namespace = namespace;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public long getLimit() {
            return limit;
        }

        public void setLimit(long limit) {
            this.limit = limit;
        }

        public Set<String> getSubnamespaceListForWhichLimitIsApplicable() {
            return subnamespaceListForWhichLimitIsApplicable;
        }

        public void setSubnamespaceListForWhichLimitIsApplicable(Set<String> subnamespaceListForWhichLimitIsApplicable) {
            this.subnamespaceListForWhichLimitIsApplicable = subnamespaceListForWhichLimitIsApplicable;
        }

        public Map<String, SubNamespaceLevelLimitData> getSubNamespaceDataMap() {
            return subNamespaceDataMap;
        }

        public void setSubNamespaceDataMap(Map<String, SubNamespaceLevelLimitData> subNamespaceDataMap) {
            this.subNamespaceDataMap = subNamespaceDataMap;
        }
    }

    private static class SubNamespaceLevelLimitData {

        public SubNamespaceLevelLimitData(String subNamespace) {
            this.subNamespace = subNamespace;
        }

        private String subNamespace;
        private long limit;
        private Set<String> orgListForWhichLimitIsApplicable = new HashSet<>();
        Map<String, OrgLevelLimitData> orgDataMap = new ConcurrentHashMap<>();

        public String getSubNamespace() {
            return subNamespace;
        }

        public void setSubNamespace(String subNamespace) {
            this.subNamespace = subNamespace;
        }

        public long getLimit() {
            return limit;
        }

        public void setLimit(long limit) {
            this.limit = limit;
        }

        public Set<String> getOrgListForWhichLimitIsApplicable() {
            return orgListForWhichLimitIsApplicable;
        }

        public void setOrgListForWhichLimitIsApplicable(Set<String> orgListForWhichLimitIsApplicable) {
            this.orgListForWhichLimitIsApplicable = orgListForWhichLimitIsApplicable;
        }

        public Map<String, OrgLevelLimitData> getOrgDataMap() {
            return orgDataMap;
        }

        public void setOrgDataMap(Map<String, OrgLevelLimitData> orgDataMap) {
            this.orgDataMap = orgDataMap;
        }
    }

    private static class OrgLevelLimitData {

        public OrgLevelLimitData(String orgName) {
            this.orgName = orgName;
        }

        private String orgName;
        private long limit;

        public String getOrgName() {
            return orgName;
        }

        public void setOrgName(String orgName) {
            this.orgName = orgName;
        }

        public long getLimit() {
            return limit;
        }

        public void setLimit(long limit) {
            this.limit = limit;
        }
    }

}
