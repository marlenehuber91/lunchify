package backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Notification {
	private Long id;
	private Long userId;
	private String entityType;
	private Long entityId;
	private String fieldChanged;
	private String oldValue;
	private String newValue;
	private String message;
	private boolean isRead;
	private LocalDateTime createdAt;
	private boolean isAdmin;
	private LocalDate originalInvoiceDate;
	private boolean isSelfmadeChange;

	public Notification() {
	}

	public Notification(Long userId, String entityType, Long entityId, String fieldChanged, String oldValue,
			String newValue, String message, boolean isAdmin, LocalDate date, boolean isSelfmadeChange) {
		this.userId = userId;
		this.entityType = entityType;
		this.entityId = entityId;
		this.fieldChanged = fieldChanged;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.message = message;
		this.isRead = false;
		this.createdAt = LocalDateTime.now();
		this.isAdmin = isAdmin;
		this.originalInvoiceDate = date;
		this.isSelfmadeChange = isSelfmadeChange;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public String getFieldChanged() {
		return fieldChanged;
	}

	public void setFieldChanged(String fieldChanged) {
		this.fieldChanged = fieldChanged;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean read) {
		isRead = read;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	public boolean getIsRead() {
		return isRead;
	}

	public LocalDate getOriginalInvoiceDate() {
		return originalInvoiceDate;
	}

	public void setOriginalInvoiceDate(LocalDate date) {
		this.originalInvoiceDate = date;
	}

	public boolean getSelfmadeChange() {
		return isSelfmadeChange;
	}

	public void setSelfmadeChange(boolean selfmade) {
		this.isSelfmadeChange = selfmade;
	}
}
