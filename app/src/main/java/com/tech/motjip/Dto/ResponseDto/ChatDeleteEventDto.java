package com.tech.motjip.Dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatDeleteEventDto {

    private Long roomId;

    private Long messageId;

    private String type;
}