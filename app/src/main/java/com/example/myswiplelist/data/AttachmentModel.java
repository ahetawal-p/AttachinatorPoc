package com.example.myswiplelist.data;

public class AttachmentModel {
	private String senderName;
	private String senderEmailAddress;
	private String date;
	private String emailContent;
	private String attchFileName;
	private boolean isTagged;
	private String tagName = "";
	private String threadId;
	private String msgId;
	private ATTACHMENT_TYPE attchType;
	
	public enum ATTACHMENT_TYPE {
		
		ARCHIVE,
		AUDIO,
		DOC,
		DRAWING,
		EXCEL,
		TEXT,
		FORM,
		FUSION,
		IMAGE,
		NOTE,
		PDF,
		POWERPOINT,
		VIDEO,
		WORD
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderEmailAddress() {
		return senderEmailAddress;
	}

	public void setSenderEmailAddress(String senderEmailAddress) {
		this.senderEmailAddress = senderEmailAddress;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getEmailContent() {
		return emailContent;
	}

	public void setEmailContent(String emailContent) {
		this.emailContent = emailContent;
	}

	public String getAttchFileName() {
		return attchFileName;
	}

	public void setAttchFileName(String attchFileName) {
		this.attchFileName = attchFileName;
	}

	public boolean isTagged() {
		return isTagged;
	}

	public void setTagged(boolean isTagged) {
		this.isTagged = isTagged;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AttachmentModel [senderName=");
		builder.append(senderName);
		builder.append(", senderEmailAddress=");
		builder.append(senderEmailAddress);
		builder.append(", date=");
		builder.append(date);
		builder.append(", emailContent=");
		builder.append(emailContent);
		builder.append(", attchFileName=");
		builder.append(attchFileName);
		builder.append(", isTagged=");
		builder.append(isTagged);
		builder.append(", tagName=");
		builder.append(tagName);
		builder.append(", threadId=");
		builder.append(threadId);
		builder.append(", msgId=");
		builder.append(msgId);
		builder.append("]");
		return builder.toString();
	}

	public ATTACHMENT_TYPE getAttchType() {
		return attchType;
	}

	public void setAttchType(ATTACHMENT_TYPE attchType) {
		this.attchType = attchType;
	}
	
	
	
}
