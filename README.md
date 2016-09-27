# ICCMATT
ICCMATT (Inter-Component Communications Modeling, Analysis, and Testing Tool) is an eclipse plug-in tool. It helps in analyzing and testing inter-component communications of android applications. It takes an android application source code as input and generates an inter-component communication graph, a security report, and test cases.

# How to use the tool?

1. Import ICCMATT in eclipse as a plugin project
2. Go to AndroidICCModel/src/androidiccmodel/handlers/SampleHandler.java
3. Modify the projectName field at line 37. Place the name of your android project.
4. Modify the output path at lines 33-35 in AndroidICCModel/src/androidiccmodel/handlers/SampleHandler.java
5. Run the project.
6. Another eclise IDE will display.
5. Run the tool by clicking ICCMATT->Run menu in the new eclipse IDE.
6. You can check the security report and test cases at the specified output paths.
7. You can also check an ICC graph at the specified output path but the graph is in graphml format.
8. The ICC graph can be best viewed with yEd Graph Editor (https://www.yworks.com/products/yed).
9. Open the graphml file using yEd.
10. In yEd, select Layout from the menu.
11. Populate labels (nodes and edges) in yEd by selecting menu Edit -> Properties Mapper -> New Configuration (Node) -> apply and New Configuration (Edge) -> apply

