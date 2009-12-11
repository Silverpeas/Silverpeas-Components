<%@ include file="checkQuizz.jsp" %>
<%
  String  csvFileName = (String) request.getAttribute("CSVFilename");
  Long  csvFileSize = (Long) request.getAttribute("CSVFileSize");
  String  csvFileURL  = (String) request.getAttribute("CSVFileURL");
%>


<html>
  <head>
    <%
      out.println(gef.getLookStyleSheet());
    %>
  </head>
  <body>
    <%
    Window window = gef.getWindow();  
    BrowseBar browseBar = window.getBrowseBar();
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();
    
    browseBar.setDomainName(quizzScc.getSpaceLabel());
      browseBar.setComponentName(quizzScc.getComponentLabel());
    browseBar.setExtraInformation(resources.getString("GML.export"));
    
    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
    %>
    <table>
      <tr>
        <td class="txtlibform"><%=resources.getString("GML.size")%> :</td>
        <td><%=FileRepositoryManager.formatFileSize(csvFileSize.longValue())%></td>
      </tr>
      <tr>
        <td class="txtlibform"><%=resources.getString("GML.csvFile")%> :</td>
        <td><a href="<%=csvFileURL%>"><%=csvFileName%></a></td>
      </tr>
    </table>
    <%
      out.println(board.printAfter());
      ButtonPane buttonPane = gef.getButtonPane();
      Button button = (Button) gef.getFormButton(resources.getString("GML.close"), "javaScript:window.close();", false);
      buttonPane.addButton(button);
      out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
      out.println(frame.printAfter());
      out.println(window.printAfter());
    %>
  </body>
</html>