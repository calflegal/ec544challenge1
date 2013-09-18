/*
 * SensorSampler.java
 *
 * Copyright (c) 2008-2010 Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.sunspotworld.demo;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.ILightSensor;
import com.sun.spot.resources.transducers.ITemperatureInput;
import com.sun.spot.util.Utils;
import javax.microedition.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * This application is the 'on SPOT' portion of the SendDataDemo. It
 * periodically samples a sensor value on the SPOT and transmits it to
 * a desktop application (the 'on Desktop' portion of the SendDataDemo)
 * where the values are displayed.
 *
 * @author: Vipul Gupta
 * modified: Ron Goldman
 */
public class SensorSampler extends MIDlet {
    
    private static final int HOST_PORT = 65;
    private static final int SAMPLE_PERIOD = 500;  // in milliseconds
    private ITemperatureInput tempSensor = (ITemperatureInput) Resources.lookup(ITemperatureInput.class);
    //our temperature array for each of the five values.
    private int[] tempArray = {0,0,0,0,0};
    
    protected void startApp() throws MIDletStateChangeException {
        RadiogramConnection rCon = null;
        Datagram dg = null;
        long now = System.currentTimeMillis();
        String ourAddress = System.getProperty("IEEE_ADDRESS");
        //ILightSensor lightSensor = (ILightSensor)Resources.lookup(ILightSensor.class);
        
        ITriColorLED led = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED7");
        
        System.out.println("Starting sensor sampler application on " + ourAddress + " ...");

	// Listen for downloads/commands over USB connection
	new com.sun.spot.service.BootloaderListenerService().getInstance().start();

        try {
            // Open up a broadcast connection to the host port
            // where the 'on Desktop' portion of this demo is listening
            rCon = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
            dg = rCon.newDatagram(50);  // only sending 12 bytes of data
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
            notifyDestroyed();
        }
        
        while (true) {
            try {
                //take a sample at every ~.5 second.
                for (int i=0; i<5; i++) {
                
                System.out.println("Sampling " + i);
                int reading = (int) tempSensor.getFahrenheit();
                tempArray[i] = reading;
                // Flash an LED to indicate a sampling event
                led.setRGB(255, 255, 255);
                led.setOn();
                Utils.sleep(50);
                led.setOff();

                

              //  System.out.println("Temp value(f) = " + reading);
                
                // Go to sleep to conserve battery
                now = System.currentTimeMillis();
                //sleep between samples
                Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - now));
                }
                
                //after five are done, move on to calculating average
                float sum = 0;
                for (int i=0; i<5; i++) {
                    sum += (float)tempArray[i];
                }
                float avg = sum/5;
                // Package the time and sensor reading into a radio datagram and send it.
                // Get the current time and sensor reading
                now = System.currentTimeMillis();
                dg.reset();
                dg.writeLong(now);
                dg.writeFloat(avg);
                rCon.send(dg);
            } catch (Exception e) {
                System.err.println("Caught " + e + " while collecting/sending sensor sample.");
            }
        }
    }
    
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }
    
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // Only called if startApp throws any exception other than MIDletStateChangeException
    }
}

