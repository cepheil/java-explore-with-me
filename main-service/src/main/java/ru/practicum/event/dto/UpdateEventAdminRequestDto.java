package ru.practicum.event.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.event.enums.AdminStateAction;


@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateEventAdminRequestDto extends EventUpdateBaseDto {
    private AdminStateAction stateAction;

}
