package dev.kreaker.cnc.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDTO {

	private boolean success;
	private String message;
	private int created;
	private int updated;
	private int failed;
	private List<String> errors = new ArrayList<>();

	public int getTotal() {
		return created + updated + failed;
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}
}
