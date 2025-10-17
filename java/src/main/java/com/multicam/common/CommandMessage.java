package com.multicam.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Command message sent to a MultiCam device.
 * <p>
 * All commands are sent as JSON over TCP socket.
 */
public class CommandMessage {
    private static final Gson gson = new GsonBuilder().create();

    /** Command type to execute */
    public CommandType command;

    /** Unix timestamp in seconds (with fractional seconds) */
    public double timestamp;

    /** ID of the device sending the command */
    public String deviceId;

    /** File name (required for GET_VIDEO and UPLOAD_TO_CLOUD commands) */
    public String fileName;

    /** Presigned S3 URL for upload (required for UPLOAD_TO_CLOUD command) */
    public String uploadUrl;

    public CommandMessage() {
        this.deviceId = "controller";
    }

    public CommandMessage(CommandType command, double timestamp, String deviceId, String fileName) {
        this(command, timestamp, deviceId, fileName, null);
    }

    public CommandMessage(CommandType command, double timestamp, String deviceId, String fileName, String uploadUrl) {
        this.command = command;
        this.timestamp = timestamp;
        this.deviceId = deviceId != null ? deviceId : "controller";
        this.fileName = fileName;
        this.uploadUrl = uploadUrl;
    }

    // Factory Methods

    /**
     * Create a START_RECORDING command.
     *
     * @return CommandMessage instance
     */
    public static CommandMessage startRecording() {
        return startRecording(null, "controller");
    }

    /**
     * Create a START_RECORDING command.
     *
     * @param timestamp Unix timestamp for scheduled recording (null for immediate)
     * @param deviceId  ID of the sending device
     * @return CommandMessage instance
     */
    public static CommandMessage startRecording(Double timestamp, String deviceId) {
        double ts = timestamp != null ? timestamp : System.currentTimeMillis() / 1000.0;
        return new CommandMessage(CommandType.START_RECORDING, ts, deviceId, null);
    }

    /**
     * Create a STOP_RECORDING command.
     *
     * @return CommandMessage instance
     */
    public static CommandMessage stopRecording() {
        return stopRecording("controller");
    }

    /**
     * Create a STOP_RECORDING command.
     *
     * @param deviceId ID of the sending device
     * @return CommandMessage instance
     */
    public static CommandMessage stopRecording(String deviceId) {
        double timestamp = System.currentTimeMillis() / 1000.0;
        return new CommandMessage(CommandType.STOP_RECORDING, timestamp, deviceId, null);
    }

    /**
     * Create a DEVICE_STATUS command.
     *
     * @return CommandMessage instance
     */
    public static CommandMessage deviceStatus() {
        return deviceStatus("controller");
    }

    /**
     * Create a DEVICE_STATUS command.
     *
     * @param deviceId ID of the sending device
     * @return CommandMessage instance
     */
    public static CommandMessage deviceStatus(String deviceId) {
        double timestamp = System.currentTimeMillis() / 1000.0;
        return new CommandMessage(CommandType.DEVICE_STATUS, timestamp, deviceId, null);
    }

    /**
     * Create a GET_VIDEO command.
     *
     * @param fileName File name to download
     * @return CommandMessage instance
     */
    public static CommandMessage getVideo(String fileName) {
        return getVideo(fileName, "controller");
    }

    /**
     * Create a GET_VIDEO command.
     *
     * @param fileName File name to download
     * @param deviceId ID of the sending device
     * @return CommandMessage instance
     */
    public static CommandMessage getVideo(String fileName, String deviceId) {
        double timestamp = System.currentTimeMillis() / 1000.0;
        return new CommandMessage(CommandType.GET_VIDEO, timestamp, deviceId, fileName);
    }

    /**
     * Create a HEARTBEAT command.
     *
     * @return CommandMessage instance
     */
    public static CommandMessage heartbeat() {
        return heartbeat("controller");
    }

    /**
     * Create a HEARTBEAT command.
     *
     * @param deviceId ID of the sending device
     * @return CommandMessage instance
     */
    public static CommandMessage heartbeat(String deviceId) {
        double timestamp = System.currentTimeMillis() / 1000.0;
        return new CommandMessage(CommandType.HEARTBEAT, timestamp, deviceId, null);
    }

    /**
     * Create a LIST_FILES command.
     * <p>
     * Note: This command may not be supported on all platforms (e.g., Android).
     *
     * @return CommandMessage instance
     */
    public static CommandMessage listFiles() {
        return listFiles("controller");
    }

    /**
     * Create a LIST_FILES command.
     * <p>
     * Note: This command may not be supported on all platforms (e.g., Android).
     *
     * @param deviceId ID of the sending device
     * @return CommandMessage instance
     */
    public static CommandMessage listFiles(String deviceId) {
        double timestamp = System.currentTimeMillis() / 1000.0;
        return new CommandMessage(CommandType.LIST_FILES, timestamp, deviceId, null);
    }

    /**
     * Create an UPLOAD_TO_CLOUD command.
     * <p>
     * Uploads the specified file to cloud storage using a presigned S3 URL.
     * File will be automatically deleted from device after successful upload.
     *
     * @param fileName  File name to upload
     * @param uploadUrl Presigned S3 URL for upload
     * @return CommandMessage instance
     */
    public static CommandMessage uploadToCloud(String fileName, String uploadUrl) {
        return uploadToCloud(fileName, uploadUrl, "controller");
    }

    /**
     * Create an UPLOAD_TO_CLOUD command.
     * <p>
     * Uploads the specified file to cloud storage using a presigned S3 URL.
     * File will be automatically deleted from device after successful upload.
     *
     * @param fileName  File name to upload
     * @param uploadUrl Presigned S3 URL for upload
     * @param deviceId  ID of the sending device
     * @return CommandMessage instance
     */
    public static CommandMessage uploadToCloud(String fileName, String uploadUrl, String deviceId) {
        double timestamp = System.currentTimeMillis() / 1000.0;
        return new CommandMessage(CommandType.UPLOAD_TO_CLOUD, timestamp, deviceId, fileName, uploadUrl);
    }

    // Serialization

    /**
     * Serialize command to JSON string.
     *
     * @return JSON string representation
     */
    public String toJson() {
        return gson.toJson(this);
    }

    /**
     * Serialize command to UTF-8 encoded bytes.
     *
     * @return UTF-8 encoded JSON bytes
     */
    public byte[] toBytes() {
        return toJson().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Deserialize command from JSON string.
     *
     * @param json JSON string to parse
     * @return CommandMessage instance
     */
    public static CommandMessage fromJson(String json) {
        return gson.fromJson(json, CommandMessage.class);
    }
}
