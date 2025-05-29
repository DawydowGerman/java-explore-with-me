package ru.practicum.event.dto.statusupdaterequest;

import lombok.*;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private StatusUpdate status;
}