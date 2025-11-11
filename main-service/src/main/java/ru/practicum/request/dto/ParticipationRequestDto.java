package ru.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.utill.DateTimeConstants;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {

    private Long id;
    @JsonFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime created;
    private Long event;
    private Long requester;
    private RequestStatus status;

}
