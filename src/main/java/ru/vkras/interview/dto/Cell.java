package ru.vkras.interview.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Cell {
    private Integer rs;
    private Integer cs;
    private String v;
}
