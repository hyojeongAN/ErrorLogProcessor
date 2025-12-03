package com.example.ErrorLogProcessor.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSettingUpdateDto {

	private boolean enableEmailNotifications;
	private boolean enableWebAppNotifications;
	private String preferredTheme;
}
