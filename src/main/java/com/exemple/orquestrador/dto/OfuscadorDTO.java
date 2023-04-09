package com.exemple.orquestrador.dto;

public class OfuscadorDTO {

	public String field;
	public Object valueEncod;
	public Object valueDecoded;
	
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public Object getValueEncod() {
		return valueEncod;
	}
	public void setValueEncod(Object valueEncod) {
		this.valueEncod = valueEncod;
	}
	public Object getValueDecoded() {
		return valueDecoded;
	}
	public void setValueDecoded(Object valueDecoded) {
		this.valueDecoded = valueDecoded;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((valueDecoded == null) ? 0 : valueDecoded.hashCode());
		result = prime * result + ((valueEncod == null) ? 0 : valueEncod.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OfuscadorDTO other = (OfuscadorDTO) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (valueDecoded == null) {
			if (other.valueDecoded != null)
				return false;
		} else if (!valueDecoded.equals(other.valueDecoded))
			return false;
		if (valueEncod == null) {
			if (other.valueEncod != null)
				return false;
		} else if (!valueEncod.equals(other.valueEncod))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "OfuscadorDTO [field=" + field + ", valueEncod=" + valueEncod + ", valueDecoded=" + valueDecoded + "]";
	}
	
	
	
		
}