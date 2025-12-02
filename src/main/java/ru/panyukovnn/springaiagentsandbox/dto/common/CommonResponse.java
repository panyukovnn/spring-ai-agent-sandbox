package ru.panyukovnn.springaiagentsandbox.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    @Builder.Default
    private UUID id = UUID.randomUUID();
    private T body;
    private String errorMessage;
    private List<ValidationError> validationErrors;
}
