package com.megaeyes.regist.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentList<T> implements List<T> {
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final List<T> list;

	public ConcurrentList(List<T> list) {
		this.list = list;
	}

	public boolean remove(Object o) {
		readWriteLock.writeLock().lock();
		boolean ret;
		try {
			ret = list.remove(o);
		} finally {
			readWriteLock.writeLock().unlock();
		}
		return ret;
	}

	public boolean add(T t) {
		readWriteLock.writeLock().lock();
		boolean ret;
		try {
			ret = list.add(t);
		} finally {
			readWriteLock.writeLock().unlock();
		}
		return ret;
	}

	public void clear() {
		readWriteLock.writeLock().lock();
		try {
			list.clear();
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public int size() {
		readWriteLock.readLock().lock();
		try {
			return list.size();
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public boolean contains(Object o) {
		readWriteLock.readLock().lock();
		try {
			return list.contains(o);
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public T get(int index) {
		readWriteLock.readLock().lock();
		try {
			return list.get(index);
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public Iterator<T> iterator() {
		readWriteLock.readLock().lock();
		try {
			return new ArrayList<T>(list).iterator();
		} finally {
			readWriteLock.readLock().unlock();
		}
	}
	
	@Override
	public void add(int index, T element) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public T remove(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T set(int index, T element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ListIterator<T> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}
	
}