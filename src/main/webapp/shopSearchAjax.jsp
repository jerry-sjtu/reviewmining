<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Insert title here</title>
<LINK REL=StyleSheet HREF="css/style.css" TYPE="text/css">
<link rel="stylesheet" href="css/demo.css" type="text/css">
<LINK REL=StyleSheet HREF="css/zTreeStyle/zTreeStyle.css" TYPE="text/css">
<script type="text/javascript" src="js/jquery-1.4.4.min.js"></script>

<script type="text/javascript" src="js/jquery.ztree.core-3.5.js"></script>
<script type="text/javascript">
var xml;
var review;
var reviewList
var reviewIndex

document.onkeydown = function (e) {
            var theEvent = window.event || e;
            var code = theEvent.keyCode || theEvent.which;
            if (code == 13&&theEvent.ctrlKey) 
			{
                $("#pairGet").click();
            }
        }

        


function shuffleArray(array) {
    for (var i = array.length - 1; i > 0; i--) {
        var j = Math.floor(Math.random() * (i + 1));
        var temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
    return array;
}		
		
function loadInfo() 
{
	$.get("shopAjaxAction.action", {shopId: $("#shopId").val()}, function(data){
		xml = data;	},"xml");
		alert("the info loaded")
	//$("#load").load("shopAjaxAction.action",{shopId: $("#shopId").val()},function(responseText,textStatus,XMLHttpRequest){
		//var jsonObject = eval("("+responseText+")");
		//$("#load").append(responseText);
	//} );
	//$("#load").load("shopAjaxAction.action",{shopId: $("#eshopId").val()});
	
}
function repairPair(obj)
{
	var parentUL = $(obj.parentNode)
	var attributeValue = parentUL.children('input')[0].value
	var opinionValue = parentUL.children('input')[1].value
	var oritation = parentUL.children('input')[2].value
	
	$.ajaxSetup({ 
	  async: true 
	  }); 
	  var paramPair = {}
	  paramPair.id = obj.id
	  paramPair.attr = attributeValue
	  paramPair.opin = opinionValue
	  paramPair.ori = oritation
	 
	$.get("aoPairRepairAction.action", paramPair);
		$("#status").contents().remove()
		$('#status').append(attributeValue+':'+opinionValue)
		
}
function addUnextractedPair(obj)
{
	
	var strValue = $('#complete').val()
	$.get("aoPairSupplementAction.action", {'id':obj.id,'pairs':strValue});
	$("#status").contents().remove()
	$('#status').append('补充完毕'+Math.random())
}
function autoInput(obj)
{
	var originStr = $('#complete').val()
	if(originStr.length>0&&originStr[originStr.length-1]!='\n')
		originStr = originStr.concat('\n')
	$('#complete').val(originStr+obj.text)
}

function showReviewPairs()
{
	$.ajaxSetup({ 
	  async: false 
	  }); 
	 if(reviewIndex>=reviewList.length)
	 {
		alert("这家店铺的评论看完了")
		return
	}
	else if(reviewIndex>=10)
	{
		alert("这家店铺的评论已经看了10条")
		return
	}
	reviewId = reviewList[reviewIndex]
	var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
	var nodes = treeObj.getNodesByParam("rid", reviewId, null);
	$("#info").contents().remove()
	var allparam=''
	for(i=0;i<nodes.length;++i)
	{
		treeNode = nodes[i]
		param = {}
		param.rid = treeNode.rid
		param.sid = treeNode.sid
		param.ssid = treeNode.ssid
		param.awi =  treeNode.awi
		param.owi =  treeNode.owi
		param.ori = treeNode.ori
		nextId = 'ul'+i
		uniPairId = 'id'+'_'+param.rid+'_'+param.sid+'_'+param.ssid+'_'+param.awi+'_'+param.owi+'_'+param.ori
		allparam = allparam+uniPairId
		$("#info").append("<ul id = "+nextId+"></ul>")
		
		$('#'+nextId).load("reviewAjaxAction.action", param)
		strstr = "<span class='red'>" +treeNode.name+"</span><br/>纠正：属性<input type='text' name='attr'>评价<input type='text' name='attr'>极性<input type='text' name='ori'><input type='button' value='纠正' id='"+uniPairId+"' onclick='repairPair(this)'>"
		
		$('#'+nextId).append("<br/>").append(strstr)
	}
	$("#reviewHightlight").load("reviewHighlightAjaxAction.action",{"pairs":allparam})
	$("#origin").contents().remove()
	$("#origin").append(" 添加新的搭配（每一行一个Pair, 空格隔开，属性在前）<br/><textarea id='complete' rows='10'  cols='80'></textarea> <br/> <input type='button' value='补充' id='"+reviewId+"' onclick='addUnextractedPair(this)'>")
	reviewIndex++
}





function zTreeOnClick(event, treeId, treeNode) 
{
	var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
	var nodes = treeObj.getNodesByParam("rid", treeNode.rid, null);
	
	if(treeNode.rid)
	{
		var param = {}
		param.rid = treeNode.rid
		param.sid = treeNode.sid
		param.ssid = treeNode.ssid
		param.awi =  treeNode.awi
		param.owi =  treeNode.owi
		param.ori = treeNode.ori
		
		//$.get("reviewAjaxAction.action", param, function(data){
		//	review = data
		//});
		
		$("#load").load("reviewAjaxAction.action", param, function(data){
			review = data
		});			
		
		
	}
    
}



function showTreeAndReview()
{
	//$("#load").append($('餐馆',xml));
	//alert(xml.getElementsByTagName('餐馆')[0]);
	reviewList = []
    reviewIndex = 0;
	var target = {}
	targetNode = xml.childNodes[xml.childNodes.length-1]
	a = elementToObject(targetNode,target);
	if(reviewList.length>10)
		reviewList = shuffleArray(reviewList)
	
	//var zTreeNodes = JSON.stringify(a);
	var zTreeNodes = a
	var setting = {
	callback: {
		onClick: zTreeOnClick
	}
	}
	
	zTreeObj = $.fn.zTree.init($("#treeDemo"),setting, zTreeNodes);
	
	
	//$('#right').load("alreadyPairs.action")
	
	alert("summary tree constructed")
	
		

	


}


function elementToObject(element, o) 
{
    var el = $(element);
    if(element.nodeName=='AOPair')
    {
		
    	el.contents().each(function(i,ele){
    		if(ele.nodeType == 3)
			{
				o.name = $(ele).text()
			}
			else if(ele.nodeType == 1)
			{
				if(ele.nodeName=='RID')
				{
					o.rid = $(ele).text()
					if(reviewList.indexOf(o.rid)==-1)
					{
						reviewList.push(o.rid)
					}
				}
				else if(ele.nodeName=='AW')
				{
					o.aw = $(ele).children('W').text()
					o.sid = $(ele).children('SI').text()
					o.ssid = $(ele).children('SSI').text()
					o.awi = $(ele).children('WI').text()
				}
				else if(ele.nodeName=='OW')
				{
					o.ow = $(ele).children('W').text()
					o.owi = $(ele).children('WI').text()
				}
				else if(ele.nodeName=='O')
				{
					o.ori = $(ele).text()
				}
			}
    	});
		
		return o
    }
	o.name = element.nodeName
   
    var i = 0;
	if(element.attributes)
		{
		    for (i ; i < element.attributes.length; i++) {
		        o[element.attributes[i].name] = element.attributes[i].value;
		    }
		}
    var children = el.children();
	
    if (children.length) {
      o.children = [];
      i = 0;
      for (i ; i < children.length; i++) 
	  {
		var target = {}
        child = children[i];
        o.children[i] = elementToObject(child, target) ;
      }
    }
    return o;
}


</script>






</head>
<body>
	<div id='header' class="head">
	   <div class="header">

		ShopId: <input type="text" name="shopId" id="shopId">
		<input type="button" value="retriev" id="xmlGet" onclick="loadInfo()">
		
		<input type="button" value="show" id="treeGet" onclick="showTreeAndReview()">
	<input type="button" value="showPair" id="pairGet" onclick="showReviewPairs()">
	</div>
	
	</div>
	<div id='left'>
	   <ul id="treeDemo" class="ztree" ></ul>
	</div>
		<div id='right'>
	   
	</div>
	<div id="mainbody">
		<div id='attention'>黄色高亮为正面情感，数字1代表; 绿色高亮为负面情感，数学2代表</div>
		<div id='status'></div>
	<div id='reviewHightlight'></div>
	<div id="info">
		
	</div>
	<div>
	========================================================
	</div>
	<div id="origin">
	</div>
	========================================================
	
	<div id="load">
	</div>
	</div>

	
</body>
</html>