package ru.vkras.interview.dto;

import lombok.Data;

import java.util.List;

@Data
public class Row {
    private List<Cell> cells;
}
