package com.helpdesk.dto;

import com.helpdesk.enums.Estado;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EstadoUpdateRequest {

    @NotNull(message = "El estado es obligatorio (ABIERTO, EN_PROCESO, RESUELTO)")
    private Estado estado;
}
