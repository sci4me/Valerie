package com.sci.valerie.util;

public final class Stack<T> {
    private static final class Node<T> {
        public Node<T> prev;
        public T data;
    }

    private Node<T> head;
    private int size;

    public Stack() {
    }

    public void push(final T t) {
        final Node<T> head = new Node<>();
        head.prev = this.head;
        head.data = t;
        this.head = head;
        this.size++;
    }

    public T pop() {
        if(this.head == null) {
            throw new RuntimeException("Attempt to pop empty stack");
        }

        final T data = this.head.data;
        this.head = this.head.prev;
        this.size--;
        return data;
    }

    public T peek() {
        if(this.head == null) {
            throw new RuntimeException("Attempt to peek empty stack");
        }

        return this.head.data;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size > 0;
    }
}