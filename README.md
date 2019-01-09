# ICCMATT
ICCMATT (Inter-Component Communications Modeling, Analysis, and Testing Tool) is an eclipse plug-in tool. It helps in analyzing and testing inter-component communications of android applications. It takes an android application source code as input and generates an inter-component communication graph, a security report, and test cases.</br>
Research papers:</br>
https://ieeexplore.ieee.org/abstract/document/7283037</br>
https://www.researchgate.net/publication/329553659


# How to use the tool?

1. Import ICCMATT (AndroidICCModel) project in eclipse
2. Use blueprints (https://github.com/tinkerpop/blueprints/wiki/GraphML-Reader-and-Writer-Library) as a plug-in dependency
3. Go to AndroidICCModel/src/androidiccmodel/handlers/SampleHandler.java
4. Modify the projectName field at line 37. Place the name of your android project. Before this, import your android project in eclipse
5. Modify the output path at lines 33-35 in AndroidICCModel/src/androidiccmodel/handlers/SampleHandler.java
6. Run the project.
7. Another eclise IDE will display.
8. Run the ICCMATT tool by clicking ICCMATT->Run menu in the new eclipse IDE.
9. You can check the security report and test cases at the specified output paths.
10. You can also check an ICC graph at the specified output path but the graph is in graphml format.
11. The ICC graph can be best viewed with yEd Graph Editor (https://www.yworks.com/products/yed).
12. Open the graphml file using yEd.
13. In yEd, select Layout from the menu.
14. Populate labels (nodes and edges) in yEd by selecting menu Edit -> Properties Mapper -> New Configuration (Node) -> apply and New Configuration (Edge) -> apply

