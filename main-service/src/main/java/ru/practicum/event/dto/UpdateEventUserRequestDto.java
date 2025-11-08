package ru.practicum.event.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.event.enums.UserStateAction;


@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateEventUserRequestDto extends EventUpdateBaseDto {
    private UserStateAction stateAction;
}
