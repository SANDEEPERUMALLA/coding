package com;

import io.lettuce.core.models.role.RedisInstance;

import java.net.InetSocketAddress;
import java.util.List;

public class RedisNode {

    private String podName;
    private final String hostName;
    private final int port;
    InetSocketAddress socketAddress;
    private RedisInstance.Role role;
    private List<Integer> slots;

    public RedisNode(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
        socketAddress = new InetSocketAddress(hostName, port);
    }

    public RedisNode(String hostName, int port, String podName) {
        this.hostName = hostName;
        this.podName = podName;
        this.port = port;
        socketAddress = new InetSocketAddress(hostName, port);
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public void setSocketAddress(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public RedisInstance.Role getRole() {
        return role;
    }

    public void setRole(RedisInstance.Role role) {
        this.role = role;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public void setSlots(List<Integer> slots) {
        this.slots = slots;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RedisNode redisNode = (RedisNode)o;

        if (port != redisNode.port)
            return false;
        if (podName != null ? !podName.equals(redisNode.podName) : redisNode.podName != null)
            return false;
        if (hostName != null ? !hostName.equals(redisNode.hostName) : redisNode.hostName != null)
            return false;
        if (socketAddress != null ? !socketAddress.equals(redisNode.socketAddress) : redisNode.socketAddress != null)
            return false;
        if (role != redisNode.role)
            return false;
        return slots != null ? slots.equals(redisNode.slots) : redisNode.slots == null;
    }

    @Override public int hashCode() {
        int result = podName != null ? podName.hashCode() : 0;
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (socketAddress != null ? socketAddress.hashCode() : 0);
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (slots != null ? slots.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "RedisNode{" + "podName='" + podName + '\'' + ", hostName='" + hostName + '\'' + ", port=" + port + ", socketAddress=" + socketAddress + ", role=" + role + ", slots=" + '}';
    }
}
