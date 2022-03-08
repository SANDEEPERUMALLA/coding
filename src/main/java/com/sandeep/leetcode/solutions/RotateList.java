package com.sandeep.leetcode.solutions;

public class RotateList {

    public ListNode rotateRight(ListNode head, int k) {
        if (head == null) {
            return null;
        }

        int listLength = length(head);
        if (listLength == 0) {
            return null;
        }

        k = k % listLength;

        int fastPointerMovementLength = listLength - (k + 1);

        ListNode n1 = head;
        while (fastPointerMovementLength >= 0) {
            n1 = n1.next;
            fastPointerMovementLength--;
        }

        ListNode n2 = head;
        while (n1.next != null) {
            n1 = n1.next;
            n2 = n2.next;
        }

        n1.next = head;
        ListNode newHead = n2.next;
        n2.next = null;
        return newHead;
    }

    private int length(ListNode head) {
        int length = 0;
        ListNode n = head;
        while (n != null) {
            n = n.next;
            length++;
        }
        return length;
    }

    private static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    private static void printList(ListNode head) {
        ListNode n = head;
        while (n != null) {
            System.out.print(n.val + "");
            n = n.next;
        }
    }

    public static void main(String[] args) {
        RotateList rotateList = new RotateList();

        ListNode n1 = new ListNode(1);
        ListNode n2 = new ListNode(2);
        ListNode n3 = new ListNode(3);
        ListNode n4 = new ListNode(4);
        ListNode n5 = new ListNode(5);
        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
        n4.next = n5;

        ListNode listNode = rotateList.rotateRight(n1, 2);
        printList(listNode);
    }

}



