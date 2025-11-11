package ru.practicum.request.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.request.enums.RequestStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {

    @NotEmpty(message = "requestIds must not be empty")
    private List<Long> requestIds;

    //Допустимые значения CONFIRMED или REJECTED
    @NotNull(message = "status must not be null")
    private RequestStatus status;

}
