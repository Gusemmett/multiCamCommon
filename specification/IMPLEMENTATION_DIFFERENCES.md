# MultiCam API - Implementation Differences

This document highlights the differences and inconsistencies found between the three MultiCam implementations:

1. **multiCam** - iOS (Swift)
2. **multiCamAndroid** - Android (Java)
3. **multiCamControllerPython** - Python Controller

---

## ⚠️ Critical Differences

### 1. LIST_FILES Command Support

**Issue:** Inconsistent implementation of the `LIST_FILES` command across platforms.

| Platform | Status | Details |
|----------|--------|---------|
| **iOS (Swift)** | ✅ **Fully Implemented** | Complete implementation with `FileMetadata` structure |
| **Android (Java)** | ❌ **Not Implemented** | `NetworkCommand.java` enum only defines 4 commands, missing `LIST_FILES` |
| **Python Controller** | ⚠️ **Partial** | Command defined but not actively used in controller logic |

**File References:**
- iOS: `/Users/angusemmett/code/multiCam/multiCam/NetworkManager.swift:27-39` (includes LIST_FILES)
- Android: `/Users/angusemmett/code/multiCamAndroid/app/src/main/java/com/emco/multicamandroid/NetworkCommand.java:7-13` (only 5 commands: START_RECORDING, STOP_RECORDING, DEVICE_STATUS, GET_VIDEO, HEARTBEAT)

**Recommendation:**
- Add `LIST_FILES` support to Android implementation
- OR document this as an iOS-only feature
- Update controller to handle platforms without LIST_FILES support

---

### 2. FileMetadata Structure

**Issue:** File metadata structure differs between implementations.

#### iOS (Swift) - Full Implementation

```swift
struct FileMetadata: Codable {
    let fileId: String
    let fileName: String
    let fileSize: Int64
    let creationDate: TimeInterval
    let modificationDate: TimeInterval
}
```

**File:** `/Users/angusemmett/code/multiCam/multiCam/NetworkManager.swift:69-75`

#### Android (Java) - No Explicit Structure

Android does not have a dedicated `FileMetadata` class. File information is only returned in responses as strings.

**File:** `/Users/angusemmett/code/multiCamAndroid/app/src/main/java/com/emco/multicamandroid/NetworkCommand.java`

#### Python Controller - Simplified

```python
# Files are represented as simple list of strings
files: Optional[List[str]] = None
```

**File:** `/Users/angusemmett/code/multiCamControllerPython/src/models/commands.py:28`

**Recommendation:**
- Standardize `FileMetadata` structure across all platforms
- All implementations should include: `fileId`, `fileName`, `fileSize`, `creationDate`, `modificationDate`

---

### 3. Response Message Field

**Issue:** Optional `message` field handling is inconsistent.

| Platform | Implementation |
|----------|----------------|
| **iOS (Swift)** | `message: String?` - optional field |
| **Android (Java)** | `message` field not defined in Response class |
| **Python Controller** | `message: Optional[str] = None` - optional field |

**Android Reference:**
`/Users/angusemmett/code/multiCamAndroid/app/src/main/java/com/emco/multicamandroid/NetworkCommand.java:42-58`

The Android `Response` class only includes:
- `deviceId`
- `status`
- `timestamp`
- `isRecording`
- `fileId`
- `fileSize`

Missing: `message` field

**Recommendation:**
- Add `message` field to Android Response class
- Use it for error descriptions and human-readable status messages

---

### 4. Device Status Enum Values

**Issue:** Different status value conventions between platforms.

#### iOS (Swift) - Snake Case

```swift
enum DeviceStatus: String, Codable {
    case ready = "ready"
    case recording = "recording"
    case stopping = "stopping"
    case error = "error"
    case scheduledRecordingAccepted = "scheduled_recording_accepted"
    case recordingStopped = "recording_stopped"
    case commandReceived = "command_received"
    case timeNotSynchronized = "time_not_synchronized"
    case fileNotFound = "file_not_found"
}
```

**File:** `/Users/angusemmett/code/multiCam/multiCam/NetworkManager.swift:15-25`

#### Android (Java) - No Enum

Android uses raw strings for status values, not an enum:

```java
response.status = "Ready";
response.status = "Recording";
response.status = "Preparing";
```

**Files:**
- `/Users/angusemmett/code/multiCamAndroid/app/src/main/java/com/emco/multicamandroid/NetworkController.java`
- `/Users/angusemmett/code/multiCamAndroid/app/src/main/java/com/emco/multicamandroid/CameraController.java`

**Observed Android Status Values:**
- "Ready" (capital R)
- "Recording" (capital R)
- "Preparing"
- "Waiting"
- "Stopping"

#### Python Controller - Accepts Multiple Formats

```python
SUCCESS_STATUSES = [
    "ready", "recording", "scheduled_recording_accepted",
    "command_received", "recording_stopped", "immediate recording started",
    "stopping", "ok", "200", "success"
]
```

**File:** `/Users/angusemmett/code/multiCamControllerPython/src/utils/constants.py:8-11`

**Recommendation:**
- Standardize on **snake_case lowercase** status values (e.g., "ready", "recording", "scheduled_recording_accepted")
- Create status enums in all platforms
- Update Android to use consistent status strings

---

### 5. Recording State Management

**Issue:** Different approaches to recording state tracking.

#### iOS (Swift) - Implicit State

iOS doesn't have an explicit recording state enum. State is implied by:
- `isRecording` boolean
- `DeviceStatus` enum values

#### Android (Java) - Explicit State Enum

```java
public enum RecordingState {
    IDLE,
    PREPARING,
    WAITING_FOR_TIME,
    RECORDING,
    STOPPING,
    PROCESSING
}
```

**File:** `/Users/angusemmett/code/multiCamAndroid/app/src/main/java/com/emco/multicamandroid/CameraController.java:32-38`

#### Python Controller - Transfer States (Different Concern)

Python has `TransferState` enum for file download/upload tracking:

```python
class TransferState(Enum):
    PENDING = "pending"
    DOWNLOADING = "downloading"
    DOWNLOADED = "downloaded"
    UPLOADING = "uploading"
    UPLOADED = "uploaded"
    FAILED = "failed"
    CANCELLED = "cancelled"
    RETRYING = "retrying"
}
```

**File:** `/Users/angusemmett/code/multiCamControllerPython/src/models/file_transfer.py:8-17`

**Recommendation:**
- These serve different purposes (device recording state vs. controller transfer state)
- Document that `RecordingState` is device-internal, not part of the wire protocol
- Keep `DeviceStatus` as the common API status enum

---

### 6. NTP Synchronization Constants

**Issue:** Different timeout and retry values.

| Constant | iOS (Swift) | Android (Java) | Python Controller |
|----------|-------------|----------------|-------------------|
| **Sync Attempts** | 4 attempts, use best 3 | 3 attempts | N/A (not implemented) |
| **NTP Timeout** | 5000ms | 5000ms | N/A |
| **Max RTT** | 500ms | Not specified | N/A |
| **Resync Interval** | 300s (5 min) | Not specified | N/A |

**iOS Reference:** `/Users/angusemmett/code/multiCam/multiCam/TimeSync.swift`
**Android Reference:** `/Users/angusemmett/code/multiCamAndroid/app/src/main/java/com/emco/multicamandroid/TimeSync.java`
**Python:** Controller doesn't implement NTP; relies on devices

**Recommendation:**
- Standardize NTP constants across device implementations
- Recommended: 4 attempts, 5s timeout, 500ms max RTT, 5-minute resync

---

### 7. File ID Format

**Issue:** Slight variations in file ID generation.

#### iOS (Swift)

```swift
// Format: video_{timestamp}
let fileName = "video_\(Int(Date().timeIntervalSince1970)).mov"
```

**File:** `/Users/angusemmett/code/multiCam/multiCam/CameraManager.swift`

#### Android (Java)

```java
// Format: {deviceId}_{unixTimeMs}
String fileId = String.format("%s_%d", deviceId, unixTimeMs);
// Example: "Mountain-A1B2C3D4_1698764400123"
```

**File:** `/Users/angusemmett/code/multiCamAndroid/app/src/main/java/com/emco/multicamandroid/NetworkController.java:356`

**Recommendation:**
- Standardize on: `{deviceId}_{unixTimeMs}`
- Update iOS to include device ID in file names
- Ensures uniqueness across devices

---

### 8. Video File Extensions

**Issue:** Different video file formats.

| Platform | File Extension | Video Codec |
|----------|----------------|-------------|
| **iOS (Swift)** | `.mov` | H.264 / HEVC |
| **Android (Java)** | `.mp4` | H.264 (MPEG-4) |
| **Python Controller** | N/A (downloads as-is) | N/A |

**Recommendation:**
- Standardize on `.mp4` (better cross-platform compatibility)
- OR document format per platform and handle in controller

---

### 9. Error Handling Differences

#### iOS (Swift) - Enum-Based

```swift
case error = "error"
case timeNotSynchronized = "time_not_synchronized"
case fileNotFound = "file_not_found"
```

Returns structured error statuses.

#### Android (Java) - String-Based

No formal error enum. Errors are returned as status strings:
- "Error: {description}"
- Status is set to generic strings

#### Python Controller - Exception-Based

Uses Python exceptions with error categorization:

```python
class ErrorCategory(Enum):
    NETWORK = "network"
    TIMEOUT = "timeout"
    AUTH = "auth"
    NOT_FOUND = "not_found"
    DISK_FULL = "disk_full"
    UNKNOWN = "unknown"
```

**File:** `/Users/angusemmett/code/multiCamControllerPython/src/models/file_transfer.py:20-27`

**Recommendation:**
- Standardize error status values in the protocol
- Add error codes to the API spec
- All platforms should return same error statuses

---

### 10. Scheduled Recording Behavior

**Issue:** Different handling of scheduled recordings.

#### iOS (Swift)
- Checks `isSynchronized` before accepting scheduled recordings
- Returns `time_not_synchronized` error if not synced
- Uses TimeSync offset for scheduling

**File:** `/Users/angusemmett/code/multiCam/multiCam/NetworkManager.swift:300-334`

#### Android (Java)
- Accepts scheduled recordings without explicit sync check
- Records immediately, then trims video to match target time
- Uses post-processing instead of precise start timing

**Files:**
- `/Users/angusemmett/code/multiCamAndroid/app/src/main/java/com/emco/multicamandroid/CameraController.java:454-493`
- `/Users/angusemmett/code/multiCamAndroid/app/src/main/java/com/emco/multicamandroid/VideoTrimmer.java`

**Difference:**
- **iOS:** Precise start time (waits until exact moment)
- **Android:** Immediate start with post-processing trim

**Recommendation:**
- Document both approaches as valid
- OR standardize on one approach
- iOS approach is more precise but may miss the moment if delayed
- Android approach is more reliable but requires post-processing

---

### 11. Network Port Discovery

#### iOS (Swift) - Fixed Port

```swift
private let port: NWEndpoint.Port = 8080
```

Port is hardcoded to 8080.

#### Android (Java) - Fixed Port

```java
private static final int SERVER_PORT = 8080;
```

Port is hardcoded to 8080.

#### Python Controller - Uses mDNS Port

Python discovers the port from mDNS service info (though typically also 8080).

**Recommendation:**
- Keep 8080 as default
- Allow configuration via environment variable or config file
- Document that port is always 8080 for mDNS-advertised services

---

## Summary of Recommendations

### High Priority (Breaking Differences)

1. **Add LIST_FILES support to Android** or document as iOS-only
2. **Standardize status enum values** (use snake_case: "ready", "recording", etc.)
3. **Add `message` field to Android Response** class
4. **Standardize file ID format**: `{deviceId}_{unixTimeMs}`
5. **Create FileMetadata structure** in all platforms

### Medium Priority (Inconsistencies)

6. **Standardize NTP constants** (4 attempts, 5s timeout, 500ms max RTT)
7. **Document error status values** and create error enums
8. **Standardize video format** (recommend `.mp4` for compatibility)

### Low Priority (Documentation)

9. **Document scheduled recording approaches** (precise start vs. post-process trim)
10. **Document recording state enums** as internal (not part of wire protocol)
11. **Document device ID format** (Noun-UUID pattern)

---

## Platform Capability Matrix

| Feature | iOS (Swift) | Android (Java) | Python Controller |
|---------|-------------|----------------|-------------------|
| START_RECORDING | ✅ | ✅ | ✅ |
| STOP_RECORDING | ✅ | ✅ | ✅ |
| DEVICE_STATUS | ✅ | ✅ | ✅ |
| GET_VIDEO | ✅ | ✅ | ✅ |
| HEARTBEAT | ✅ | ✅ | ✅ |
| LIST_FILES | ✅ | ❌ | ⚠️ Defined but unused |
| NTP Sync | ✅ | ✅ | ❌ Relies on devices |
| mDNS Discovery | ✅ | ✅ | ✅ |
| File Metadata | ✅ Full | ❌ | ⚠️ Simplified |
| Scheduled Recording | ✅ Precise | ✅ Post-process | ✅ Sends commands |
| Video Format | .mov | .mp4 | N/A |
| Status Enum | ✅ | ❌ Strings only | ✅ |
| Error Enum | ✅ | ❌ | ✅ |

---

## Action Items for Standardization

### For Android Implementation

- [ ] Add `LIST_FILES` command support
- [ ] Create `DeviceStatus` enum with snake_case values
- [ ] Add `message` field to `Response` class
- [ ] Create `FileMetadata` class
- [ ] Update file ID format to include device ID prefix
- [ ] Consider switching to precise scheduled recording (instead of post-trim)

### For iOS Implementation

- [ ] Update file ID format to: `{deviceId}_{unixTimeMs}`
- [ ] Verify status values match spec (already good)

### For Python Controller

- [ ] Add proper error handling for LIST_FILES (platform detection)
- [ ] Document NTP is device-side only

### For All Implementations

- [ ] Standardize NTP constants
- [ ] Create common test suite for protocol compliance
- [ ] Version the protocol specification
- [ ] Add protocol version field to messages for future compatibility

---

## Testing Recommendations

To ensure compatibility across implementations:

1. **Cross-Platform Integration Tests**
   - iOS device with Python controller
   - Android device with Python controller
   - Mixed iOS/Android with Python controller

2. **Protocol Compliance Tests**
   - JSON message format validation
   - Binary protocol validation
   - Error handling consistency

3. **Time Sync Accuracy Tests**
   - Measure actual sync accuracy across platforms
   - Test scheduled recording precision

4. **File Transfer Tests**
   - Large file transfers (>1GB)
   - Network interruption handling
   - Concurrent downloads

---

## Version History

- **v1.0** - Initial analysis (2024-10-15)
  - Identified 11 major differences between implementations
  - Documented platform capability matrix
  - Provided standardization recommendations
