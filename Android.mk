# Copyright 2008 The Android Open Source Project
#
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_MODULE := OnlyInput_above_4.0
include $(BUILD_JAVA_LIBRARY)

#include $(CLEAR_VARS)
#ALL_PREBUILT += $(TARGET_OUT)/bin/input
#$(TARGET_OUT)/bin/input : $(LOCAL_PATH)/input | $(ACP)
#	$(transform-prebuilt-to-target)
