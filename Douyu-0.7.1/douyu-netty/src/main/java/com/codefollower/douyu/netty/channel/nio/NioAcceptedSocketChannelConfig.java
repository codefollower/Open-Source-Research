/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.codefollower.douyu.netty.channel.nio;

import java.net.Socket;
import java.net.SocketException;
import java.util.Map;


import com.codefollower.douyu.logging.InternalLogger;
import com.codefollower.douyu.logging.InternalLoggerFactory;
import com.codefollower.douyu.netty.channel.AdaptiveReceiveBufferSizePredictorFactory;
import com.codefollower.douyu.netty.channel.ChannelException;
import com.codefollower.douyu.netty.channel.DefaultChannelConfig;
import com.codefollower.douyu.netty.channel.ReceiveBufferSizePredictor;
import com.codefollower.douyu.netty.channel.ReceiveBufferSizePredictorFactory;
import com.codefollower.douyu.netty.util.internal.ConversionUtil;

/**
 * The default {@link NioSocketChannelConfig} implementation.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev$, $Date$
 *
 */
class NioAcceptedSocketChannelConfig extends DefaultChannelConfig {

    private static final InternalLogger logger =
        InternalLoggerFactory.getInstance(NioAcceptedSocketChannelConfig.class);

    private static final ReceiveBufferSizePredictorFactory DEFAULT_PREDICTOR_FACTORY =
        new AdaptiveReceiveBufferSizePredictorFactory();

    private volatile int writeBufferHighWaterMark = 64 * 1024;
    private volatile int writeBufferLowWaterMark  = 32 * 1024;
    private volatile ReceiveBufferSizePredictor predictor;
    private volatile ReceiveBufferSizePredictorFactory predictorFactory = DEFAULT_PREDICTOR_FACTORY;
    private volatile int writeSpinCount = 16;
    
    private final Socket socket;

    /**
     * Creates a new instance.
     */
    NioAcceptedSocketChannelConfig(Socket socket) {
    	if (socket == null) {
            throw new NullPointerException("socket");
        }
        this.socket = socket;
    }

    @Override
    public void setOptions(Map<String, Object> options) {
        super.setOptions(options);
        if (getWriteBufferHighWaterMark() < getWriteBufferLowWaterMark()) {
            // Recover the integrity of the configuration with a sensible value.
            setWriteBufferLowWaterMark0(getWriteBufferHighWaterMark() >>> 1);
            // Notify the user about misconfiguration.
            logger.warn(
                    "writeBufferLowWaterMark cannot be greater than " +
                    "writeBufferHighWaterMark; setting to the half of the " +
                    "writeBufferHighWaterMark.");
        }
    }

    @Override
    public boolean setOption(String key, Object value) {
        if (setOption0(key, value)) {
            return true;
        }

        if (key.equals("writeBufferHighWaterMark")) {
            setWriteBufferHighWaterMark0(ConversionUtil.toInt(value));
        } else if (key.equals("writeBufferLowWaterMark")) {
            setWriteBufferLowWaterMark0(ConversionUtil.toInt(value));
        } else if (key.equals("writeSpinCount")) {
            setWriteSpinCount(ConversionUtil.toInt(value));
        } else if (key.equals("receiveBufferSizePredictorFactory")) {
            setReceiveBufferSizePredictorFactory((ReceiveBufferSizePredictorFactory) value);
        } else if (key.equals("receiveBufferSizePredictor")) {
            setReceiveBufferSizePredictor((ReceiveBufferSizePredictor) value);
        } else {
            return false;
        }
        return true;
    }
    
    public boolean setOption0(String key, Object value) {
        if (super.setOption(key, value)) {
            return true;
        }

        if (key.equals("receiveBufferSize")) {
            setReceiveBufferSize(ConversionUtil.toInt(value));
        } else if (key.equals("sendBufferSize")) {
            setSendBufferSize(ConversionUtil.toInt(value));
        } else if (key.equals("tcpNoDelay")) {
            setTcpNoDelay(ConversionUtil.toBoolean(value));
        } else if (key.equals("keepAlive")) {
            setKeepAlive(ConversionUtil.toBoolean(value));
        } else if (key.equals("reuseAddress")) {
            setReuseAddress(ConversionUtil.toBoolean(value));
        } else if (key.equals("soLinger")) {
            setSoLinger(ConversionUtil.toInt(value));
        } else if (key.equals("trafficClass")) {
            setTrafficClass(ConversionUtil.toInt(value));
        } else {
            return false;
        }
        return true;
    }

    public int getWriteBufferHighWaterMark() {
        return writeBufferHighWaterMark;
    }

    public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        if (writeBufferHighWaterMark < getWriteBufferLowWaterMark()) {
            throw new IllegalArgumentException(
                    "writeBufferHighWaterMark cannot be less than " +
                    "writeBufferLowWaterMark (" + getWriteBufferLowWaterMark() + "): " +
                    writeBufferHighWaterMark);
        }
        setWriteBufferHighWaterMark0(writeBufferHighWaterMark);
    }

    private void setWriteBufferHighWaterMark0(int writeBufferHighWaterMark) {
        if (writeBufferHighWaterMark < 0) {
            throw new IllegalArgumentException(
                    "writeBufferHighWaterMark: " + writeBufferHighWaterMark);
        }
        this.writeBufferHighWaterMark = writeBufferHighWaterMark;
    }

    public int getWriteBufferLowWaterMark() {
        return writeBufferLowWaterMark;
    }

    public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        if (writeBufferLowWaterMark > getWriteBufferHighWaterMark()) {
            throw new IllegalArgumentException(
                    "writeBufferLowWaterMark cannot be greater than " +
                    "writeBufferHighWaterMark (" + getWriteBufferHighWaterMark() + "): " +
                    writeBufferLowWaterMark);
        }
        setWriteBufferLowWaterMark0(writeBufferLowWaterMark);
    }

    private void setWriteBufferLowWaterMark0(int writeBufferLowWaterMark) {
        if (writeBufferLowWaterMark < 0) {
            throw new IllegalArgumentException(
                    "writeBufferLowWaterMark: " + writeBufferLowWaterMark);
        }
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;
    }

    public int getWriteSpinCount() {
        return writeSpinCount;
    }

    public void setWriteSpinCount(int writeSpinCount) {
        if (writeSpinCount <= 0) {
            throw new IllegalArgumentException(
                    "writeSpinCount must be a positive integer.");
        }
        this.writeSpinCount = writeSpinCount;
    }

    public ReceiveBufferSizePredictor getReceiveBufferSizePredictor() {
        ReceiveBufferSizePredictor predictor = this.predictor;
        if (predictor == null) {
            try {
                this.predictor = predictor = getReceiveBufferSizePredictorFactory().getPredictor();
            } catch (Exception e) {
                throw new ChannelException(
                        "Failed to create a new " +
                        ReceiveBufferSizePredictor.class.getSimpleName() + '.',
                        e);
            }
        }
        return predictor;
    }

    public void setReceiveBufferSizePredictor(
            ReceiveBufferSizePredictor predictor) {
        if (predictor == null) {
            throw new NullPointerException("predictor");
        }
        this.predictor = predictor;
    }

    public ReceiveBufferSizePredictorFactory getReceiveBufferSizePredictorFactory() {
        return predictorFactory;
    }

    public void setReceiveBufferSizePredictorFactory(ReceiveBufferSizePredictorFactory predictorFactory) {
        if (predictorFactory == null) {
            throw new NullPointerException("predictorFactory");
        }
        this.predictorFactory = predictorFactory;
    }
    
    public int getReceiveBufferSize() {
        try {
            return socket.getReceiveBufferSize();
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public int getSendBufferSize() {
        try {
            return socket.getSendBufferSize();
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public int getSoLinger() {
        try {
            return socket.getSoLinger();
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public int getTrafficClass() {
        try {
            return socket.getTrafficClass();
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public boolean isKeepAlive() {
        try {
            return socket.getKeepAlive();
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public boolean isReuseAddress() {
        try {
            return socket.getReuseAddress();
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public boolean isTcpNoDelay() {
        try {
            return socket.getTcpNoDelay();
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public void setKeepAlive(boolean keepAlive) {
        try {
            socket.setKeepAlive(keepAlive);
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public void setPerformancePreferences(
            int connectionTime, int latency, int bandwidth) {
        socket.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        try {
            socket.setReceiveBufferSize(receiveBufferSize);
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public void setReuseAddress(boolean reuseAddress) {
        try {
            socket.setReuseAddress(reuseAddress);
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public void setSendBufferSize(int sendBufferSize) {
        try {
            socket.setSendBufferSize(sendBufferSize);
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public void setSoLinger(int soLinger) {
        try {
            if (soLinger < 0) {
                socket.setSoLinger(false, 0);
            } else {
                socket.setSoLinger(true, soLinger);
            }
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        try {
            socket.setTcpNoDelay(tcpNoDelay);
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }

    public void setTrafficClass(int trafficClass) {
        try {
            socket.setTrafficClass(trafficClass);
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
    }
}
