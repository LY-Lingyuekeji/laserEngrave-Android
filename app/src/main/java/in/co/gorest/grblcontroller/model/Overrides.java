package in.co.gorest.grblcontroller.model;

public enum Overrides {
    CMD_FEED_OVR_RESET,             // 0x90
    CMD_FEED_OVR_COARSE_PLUS,       // 0x91
    CMD_FEED_OVR_COARSE_MINUS,      // 0x92
    CMD_FEED_OVR_FINE_PLUS ,        // 0x93
    CMD_FEED_OVR_FINE_MINUS ,       // 0x94
    CMD_RAPID_OVR_RESET,            // 0x95
    CMD_RAPID_OVR_MEDIUM,           // 0x96
    CMD_RAPID_OVR_LOW,              // 0x97
    CMD_SPINDLE_OVR_RESET,          // 0x99
    CMD_SPINDLE_OVR_COARSE_PLUS,    // 0x9A
    CMD_SPINDLE_OVR_COARSE_MINUS,   // 0x9B
    CMD_SPINDLE_OVR_FINE_PLUS,      // 0x9C
    CMD_SPINDLE_OVR_FINE_MINUS,     // 0x9D
    CMD_TOGGLE_SPINDLE,             // 0x9E
    CMD_TOGGLE_FLOOD_COOLANT,       // 0xA0
    CMD_TOGGLE_MIST_COOLANT,        // 0xA1
}

