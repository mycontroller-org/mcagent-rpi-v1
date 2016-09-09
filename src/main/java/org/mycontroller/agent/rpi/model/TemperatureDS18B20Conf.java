/*
 * Copyright 2016 Jeeva Kandasamy (jkandasa@gmail.com)
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.agent.rpi.model;

import org.mycontroller.agent.rpi.devices.TemperatureDS18B20;
import org.mycontroller.agent.rpi.mqtt.AgentRawMessageQueue;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.provider.mc.McpRawMessage;

import com.pi4j.temperature.TemperatureScale;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Slf4j
public class TemperatureDS18B20Conf extends W1GenericConf {
    public static final String KEY_SCALE = "scale";
    private TemperatureScale scale; //Can be either F or C

    public TemperatureDS18B20Conf(Device device) {
        super(device);
        try {
            scale = TemperatureScale.valueOf(getValue(device.getProperties(), KEY_SCALE,
                    TemperatureScale.CELSIUS.name())
                    .toUpperCase());
        } catch (Exception ex) {
            _logger.warn("Unknown scale:[{}] for the {}, taking CELSIUS", getValue(device.getProperties(), KEY_SCALE),
                    device);
        }
        if (scale == null) {
            scale = TemperatureScale.CELSIUS;
        }
    }

    @Override
    public void aboutMe() {
        McpRawMessage message = super.getPresentationMessage();
        message.setSubType(MESSAGE_TYPE_PRESENTATION.S_TEMP.name());
        message.setPayload(getName());
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

    @Override
    public void sendSensorTypes() {
        //Send Rate message
        McpRawMessage message = getMcpRawMessage();
        message.setMessageType(MESSAGE_TYPE.C_REQ);
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());
        message = getMcpRawMessageId();
        message.setMessageType(MESSAGE_TYPE.C_REQ);
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

    @Override
    public McpRawMessage getMcpRawMessage() {
        return super.getMcpRawMessage(MESSAGE_TYPE_SET_REQ.V_TEMP);
    }

    @Override
    public void sendMeasurments() {
        McpRawMessage message = getMcpRawMessage();
        message.setPayload(new TemperatureDS18B20().getTemperature(this));
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }
}
