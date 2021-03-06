/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.waterdays.ble;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    // Sample Characteristics.
    public static String MIO_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String SET_ADC_CURRENT_TRESHOLD_HDLE = "0000fe41-8e22-4541-9d4c-21edae82ed19";
    public static String SHUTDOWN_CURRENT_HDLE = "0000fe42-8e22-4541-9d4c-21edae82ed19";
    public static String START_UP_STEP_HDLE = "0000fe43-8e22-4541-9d4c-21edae82ed19";
    public static String START_UP_TIME_HDLE = "0000fe44-8e22-4541-9d4c-21edae82ed19";
    public static String DEAD_ZONE_HDLE = "0000fe45-8e22-4541-9d4c-21edae82ed19";
    public static String OPEN_THRESHOLD_HDLE = "0000fe46-8e22-4541-9d4c-21edae82ed19";
    public static String CLOSE_THRESHOLD_HDLE = "0000fe47-8e22-4541-9d4c-21edae82ed19";
    public static String OPEN_MOTOR_HDLE = "0000fe48-8e22-4541-9d4c-21edae82ed19";
    public static String CLOSE_MOTOR_HDLE = "0000fe49-8e22-4541-9d4c-21edae82ed19";
    public static String SENSITIVITY_HDLE = "0000fe4a-8e22-4541-9d4c-21edae82ed19";
    public static String BRAKE_MOTOR_TIME_HDLE = "0000fe4b-8e22-4541-9d4c-21edae82ed19";
    public static String BRAKE_MOTOR_HDLE = "0000fe4c-8e22-4541-9d4c-21edae82ed19";
    public static String SENS_OPTIONS = "0000fe4d-8e22-4541-9d4c-21edae82ed19";

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String MY_TEST_MEASUREMENT = "00002a00-0000-1000-8000-00805f9b34fb";
    // Sample Commands.
    public static String READ = "READ";
    public static String WRITE = "WRITE";
    public static String NOTIFY = "NOTIFY";


    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("00001810-0000-1000-8000-00805f9b34fb", "Что-то моё");
        attributes.put("0000fe40-cc7a-482a-984a-7f2ed5b3e58f", "Наша кастомная характеристика");
        // Sample Characteristics.
        attributes.put(MIO_MEASUREMENT, "MIO Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");

        attributes.put(SET_ADC_CURRENT_TRESHOLD_HDLE, "Set ADC Current Treshold Hdle");
        attributes.put(SHUTDOWN_CURRENT_HDLE, "Shutdown Current Hdle");
        attributes.put(START_UP_STEP_HDLE, "Start Up Step Hdle");
        attributes.put(START_UP_TIME_HDLE, "Start Up Time Hdle");
        attributes.put(DEAD_ZONE_HDLE, "Dead Zone Hdle");
        attributes.put(OPEN_THRESHOLD_HDLE, "Open Threshold Hdle");
        attributes.put(CLOSE_THRESHOLD_HDLE, "Close Threshold Hdle");
        attributes.put(OPEN_MOTOR_HDLE, "Motor Open Hdle");
        attributes.put(CLOSE_MOTOR_HDLE, "Motor Close Hdle");
        attributes.put(SENSITIVITY_HDLE, "Sensitivity Hdle");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
