<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Insert title here</title>
<LINK REL=StyleSheet HREF="css/style.css" TYPE="text/css" MEDIA=screen>
</head>
<body>
	<form name="input" action="shopAction.action">
		ShopId: <input type="text" name="shopId">
		<input type="submit" value="Submit">
	</form>
	
	
	<form name="input" action="shopReviewAction.action">
		ShopId: <input type="text" name="shopId">
		Tag: <input type="text" name="labelTag">
		Ori:<input type="text" name="ori">
		<input type="submit" value="Submit">
	</form>
	
	<s:if test="null != summaryText">
		<div id="summaryTreePanel">
			<s:property value="summaryText"/>
		</div>
	</s:if>
	

</body>
</html>