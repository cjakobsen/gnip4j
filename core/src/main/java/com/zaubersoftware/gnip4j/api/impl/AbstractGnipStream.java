/**
 * Copyright (c) 2011 Zauber S.A. <http://www.zaubersoftware.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zaubersoftware.gnip4j.api.impl;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaubersoftware.gnip4j.api.GnipStream;
import com.zaubersoftware.gnip4j.api.StreamNotification;
import com.zaubersoftware.gnip4j.api.exception.GnipException;
import com.zaubersoftware.gnip4j.api.exception.TransportGnipException;
import com.zaubersoftware.gnip4j.api.model.Activity;

/**
 * Abstract skeleton implementation of the {@link GnipStream} interface.
 * This is the base class of all the {@link GnipStream} implementations.
 *
 * @author Guido Marucci Blas
 * @since Apr 29, 2011
 */
public abstract class AbstractGnipStream implements GnipStream {
    private final Lock lock = new ReentrantLock();
    private final Condition emptyCondition  = lock.newCondition(); 
    private final AtomicBoolean streamClosed = new AtomicBoolean(false);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private StreamNotification notification = new StreamNotification() {
        @Override
        public void notify(final Activity activity, final GnipStream stream) {
            // nothing to do
        }

        @Override
        public void notifyConnectionError(TransportGnipException e) {
            // TODO: Auto-generated method stub
            
        }

        @Override
        public void notifyReConnectionError(GnipException e) {
            // TODO: Auto-generated method stub
            
        }

        @Override
        public void notifyReConnection(int attempt, long waitTime) {
            // TODO: Auto-generated method stub
            
        }
        
        
    };
    
    @Override
    public final void addObserver(final StreamNotification notification) {
        if(notification != null) {
            this.notification  = notification;
        }
    }

    @Override
    public final void close() {
        try {
            doClose();
        } finally {
            lock.lock();
            try { 
                streamClosed.set(true);
                emptyCondition.signalAll();
            } catch(final Throwable t) {
                logger.error("decrementing active jobs. should not happen ", t);
            } finally {
                lock.unlock();
            }
        }
    }

    /**  template method for close */
    protected void doClose() {
        
    }

    @Override
    public final void await() throws InterruptedException {
        lock.lock();
        try {
            while(!streamClosed.get()) {
                emptyCondition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public StreamNotification getNotification() {
        return notification;
    }
}
