package ru.vkras.interview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import ru.vkras.interview.dto.Cell;
import ru.vkras.interview.dto.Row;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
public class Application {

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = getJsonFromFileAsString("table.json", mapper);
        List<Row> rowList = new ArrayList<>();
        mapToTable(Collections.singletonList(jsonNode), false, rowList);
        System.out.println(mapper.writeValueAsString(rowList));
    }

    // парсим json из ресурсов
    private static JsonNode getJsonFromFileAsString(String fileName, ObjectMapper mapper) throws IOException {
        InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(fileName);
        return mapper.readValue(in, JsonNode.class);
    }

    // основной алгоритм
    private static void mapToTable(List<JsonNode> nodes,
                                   Boolean isChild,
                                   List<Row> rowList) {
        Row row = new Row();
        List<Cell> cells = new ArrayList<>();
        if (isChild) {
            cells.add(null);
        }
        List<JsonNode> objectNodes = new ArrayList<>();
        nodes.forEach(node -> {
            List<String> fields = IteratorUtils.toList(node.fieldNames());
            fields.forEach(s -> {
                if (node.get(s).getClass().equals(TextNode.class)){
                    if (!s.equals("head")){
                        cells.add(addCell(node, fields, s));
                    }
                }
                else {
                    objectNodes.add(node.get(s));
                }
            });
        });
        if (CollectionUtils.isNotEmpty(objectNodes)){
            objectNodes.forEach(node -> cells.add(addObjectCell(node)));
            row.setCells(cells);
            rowList.add(row);
            mapToTable(objectNodes, true, rowList);
        }
        else {
            row.setCells(cells);
            rowList.add(row);
        }
    }

    // добавляем cell текстовый для текстовой ноды
    private static Cell addCell(JsonNode node,
                                List<String> fields,
                                String s) {
        Cell cell = new Cell();
        JsonNode childNode = node.get(s);
        if (s.contains("t")) {
            if (!fields.contains("head")) {
                cell.setRs(getCountOfRs(node));
                cell.setV(childNode.asText());
                cell.setCs(1);
            } else {
                cell.setRs(1);
                cell.setV(childNode.asText());
                cell.setCs(1);
            }
        }
        return cell;
    }

    // добавляем cell для ноды-объекта
    private static Cell addObjectCell(JsonNode node){
        JsonNode childNode = node.get("head");
        Cell cell = new Cell();
        cell.setRs(1);
        cell.setV(childNode.asText());
        cell.setCs(getCountOfCs(node));
        return cell;
    }

    // Вычисляем глубину ноды
    private static Integer getCountOfRs(JsonNode node) {
        Integer rs = 0;
        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> stringJsonNodeEntry = it.next();
                
                if (stringJsonNodeEntry.getValue().getClass().equals(ObjectNode.class)) {
                    Integer tempDepth = getCountOfRs(stringJsonNodeEntry.getValue());
                    if (tempDepth > rs){
                        rs = tempDepth;
                    }
                }
        }
        return 1 + rs;
    }

    // Вычисляем cs
    private static Integer getCountOfCs(JsonNode node) {
        List<String> textFields = getAllTextFields(node, new ArrayList<>());
        return Math.toIntExact(textFields.stream()
                .filter(s -> s.contains("t"))
                .count());
    }

    // получаем все текстовые поля json для расчета cs
    private static List<String> getAllTextFields(JsonNode node, List<String> fieldNames){
        node.fields().forEachRemaining(stringJsonNodeEntry -> {
            if (stringJsonNodeEntry.getValue().getClass().equals(ObjectNode.class)){
                getAllTextFields(stringJsonNodeEntry.getValue(), fieldNames);
            }
            else {
                fieldNames.add(stringJsonNodeEntry.getKey());
            }
        });
        return  fieldNames;
    }
}
