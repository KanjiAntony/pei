package nars.gui.graph;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.model.mxGeometry;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import nars.core.NAR;
import nars.graph.NARGraph;
import static nars.graph.NARGraph.IncludeEverything;
import nars.gui.NSlider;
import nars.io.TextInput;
import nars.language.Term;
import nars.storage.Memory;
import org.jgrapht.ext.JGraphXAdapter;
import processing.core.PApplet;
import static processing.core.PConstants.DOWN;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.RIGHT;
import static processing.core.PConstants.UP;


class applet extends PApplet implements ActionListener //(^break,0_0)! //<0_0 --> deleted>>! (--,<0_0 --> deleted>>)!
{

///////////////HAMLIB
//processingjs compatibility layer
    int mouseScroll = 0;

    ProcessingJs processingjs = new ProcessingJs();
//Hnav 2D navigation system   
    Hnav hnav = new Hnav();
//Object
    float selection_distance = 10;
    public float maxNodeSize = 50f;

    Hsim hsim = new Hsim();

    Hamlib hamlib = new Hamlib();

    Obj lastclicked = null;

    public Button getBack;
    public Button conceptsView;
    public Button memoryView;
    public Button fetchMemory;
    Memory mem = null;

    public int mode = 0;
    
    boolean showBeliefs = false;
    
    float sx = 800;
    float sy = 800;

    long lasttime = -1;

    boolean autofetch = true;
    private int MAX_UNSELECTED_LABEL_LENGTH = 32;
    private boolean updateNext;
    float nodeSize = 10;
    
    NARGraph graph;
    JGraphXAdapter layout;

    public void mouseScrolled() {
        hamlib.mouseScrolled();
    }

    public void keyPressed() {
        hamlib.keyPressed();
    }

    public void mouseMoved() {
        hamlib.mouseMoved();
    }

    public void mouseReleased() {
        hamlib.mouseReleased();
    }

    public void mouseDragged() {
        hamlib.mouseDragged();
    }

    public void mousePressed() {
        hamlib.mousePressed();
    }

    @Override
    public void draw() {
        hamlib.Update(128, 138, 128);
    }

    void hsim_ElemClicked(Obj i) {
        lastclicked = i;
    }

    void hsim_ElemDragged(Obj i) {
    }

    void hrend_DrawBegin() {
    }

    void hrend_DrawEnd() {
        //fill(0);
        //text("Hamlib simulation system demonstration", 0, -5);
        //stroke(255, 255, 255);
        //noStroke();
        if (lastclicked != null) {
            fill(255, 0, 0);
            ellipse(lastclicked.x, lastclicked.y, 10, 10);
        }
    }


    @Override
    public void setup() {  
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
        background(0);
        
    }

    public void hsim_Draw(Obj oi) {
        text(oi.name.toString(), oi.x, oi.y);
    }

    
    void drawArrowAngle(float cx, float cy, float len, float angle){
      pushMatrix();
      translate(cx, cy);
      rotate(radians(angle));
      line(0,0,len, 0);
      line(len, 0, len - 8, -8);
      line(len, 0, len - 8, 8);
      popMatrix();
    }

    void drawArrow(float x1, float y1, float x2, float y2) {
        float cx = (x1+x2)/2f;
        float cy = (y1+y2)/2f;
        float len = (float)Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
        float a = (float)(Math.atan2(y2-y1,x2-x1)*180.0/Math.PI);
        
        drawArrowAngle(x1, y1, len, a);
    }
    
    public int getColor(String s) {            
        double hue = (((double)s.hashCode()) / Integer.MAX_VALUE);
        return Color.getHSBColor((float)hue,0.7f,0.8f).getRGB();        
    }
    
    public void drawit() {
        
        
        background(0, 0, 0);


        
        //  line(elem1.x, elem1.y, elem2.x, elem2.y);
        for (Object edge : graph.edgeSet()) {
                  
            int rgb = getColor(edge.getClass().getSimpleName());
            stroke(rgb, 230f);            
            strokeWeight(linkWeight);                
            
            
            
            Object sourceVertex = graph.getEdgeSource(edge);
            mxGeometry sourcePoint = layout.getCellGeometry(layout.getVertexToCellMap().get(sourceVertex));
            
            Object targetVertex = graph.getEdgeTarget(edge);                          mxGeometry targetPoint = layout.getCellGeometry(layout.getVertexToCellMap().get(targetVertex));

            float x1 = (float)sourcePoint.getCenterX();
            float y1 = (float)sourcePoint.getCenterY();
            float x2 = (float)targetPoint.getCenterX();
            float y2 = (float)targetPoint.getCenterY();
            float cx = (x1 + x2) / 2.0f;
            float cy = (y1 + y2) / 2.0f;
            drawArrow(x1, y1, x2, y2);
            text(edge.toString(), cx, cy);            
        }

        strokeWeight(0);        
        for (Object vertex : graph.vertexSet()) {            
            Object cell = layout.getVertexToCellMap().get(vertex);
            mxGeometry b = layout.getCellGeometry(cell);            
            if (b == null) continue;
            
            int rgb = getColor(vertex.getClass().getSimpleName());
            fill(rgb, 130f);
            
            float x = (float)b.getCenterX();
            float y = (float)b.getCenterY();
            double w = b.getWidth();
            double h = b.getHeight();
            ellipse(x, y, 90, 90);            
            
            fill(255,255,255);            
            text(vertex.toString(), x, y);
        }
                    
    }
    
    private static final float linkWeight = 3.0f;


    public void actionPerformed(ActionEvent e) {
        String command = ((Button) e.getSource()).getActionCommand();
        if (command.equals("Fetch")) {
            autofetch = true;
            return;
        }
    }

    void setUpdateNext() {
        updateNext = true;
    }

    class ProcessingJs {

        ProcessingJs() {
            addMouseWheelListener(new java.awt.event.MouseWheelListener() {
                public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                    mouseScroll = -evt.getWheelRotation();
                    mouseScrolled();
                }
            }
            );
        }
    }

    class Hnav {

        private float savepx = 0;
        private float savepy = 0;
        private int selID = 0;
        private float zoom = 1.0f;
        private float difx = 0;
        private float dify = 0;
        private int lastscr = 0;
        private boolean EnableZooming = true;
        private float scrollcamspeed = 1.1f;

        float MouseToWorldCoordX(int x) {
            return 1 / zoom * (x - difx - width / 2);
        }

        float MouseToWorldCoordY(int y) {
            return 1 / zoom * (y - dify - height / 2);
        }
        private boolean md = false;

        void mousePressed() {
            md = true;
            if (mouseButton == RIGHT) {
                savepx = mouseX;
                savepy = mouseY;
            }
        }

        void mouseReleased() {
            md = false;
        }

        void mouseDragged() {
            if (mouseButton == RIGHT) {
                difx += (mouseX - savepx);
                dify += (mouseY - savepy);
                savepx = mouseX;
                savepy = mouseY;
            }
        }
        private float camspeed = 20.0f;
        private float scrollcammult = 0.92f;
        boolean keyToo = true;

        void keyPressed() {
            if ((keyToo && key == 'w') || keyCode == UP) {
                dify += (camspeed);
            }
            if ((keyToo && key == 's') || keyCode == DOWN) {
                dify += (-camspeed);
            }
            if ((keyToo && key == 'a') || keyCode == LEFT) {
                difx += (camspeed);
            }
            if ((keyToo && key == 'd') || keyCode == RIGHT) {
                difx += (-camspeed);
            }
            if (!EnableZooming) {
                return;
            }
            if (key == '-' || key == '#') {
                float zoomBefore = zoom;
                zoom *= scrollcammult;
                difx = (difx) * (zoom / zoomBefore);
                dify = (dify) * (zoom / zoomBefore);
            }
            if (key == '+') {
                float zoomBefore = zoom;
                zoom /= scrollcammult;
                difx = (difx) * (zoom / zoomBefore);
                dify = (dify) * (zoom / zoomBefore);
            }
        }

        void Init() {
            difx = -width / 2;
            dify = -height / 2;
        }

        void mouseScrolled() {
            if (!EnableZooming) {
                return;
            }
            float zoomBefore = zoom;
            if (mouseScroll > 0) {
                zoom *= scrollcamspeed;
            } else {
                zoom /= scrollcamspeed;
            }
            difx = (difx) * (zoom / zoomBefore);
            dify = (dify) * (zoom / zoomBefore);
        }

        void Transform() {
            translate(difx + 0.5f * width, dify + 0.5f * height);
            scale(zoom, zoom);
        }
    }

    public class Obj {

        public float x;
        public float y;
        public final int type;
        public final Term name;
        private final long creationTime;

        public Obj(int index, int level, Term Name, int Mode) {
            this(index, level, Name, Mode, -1);
        }

        private int rowHeight = (int)(maxNodeSize);
        private int colWidth = (int)(maxNodeSize);
        
        public Obj(int index, int level, Term term, int type, long creationTime) {
            
            if (mode == 1) {
                this.y = -200 - (index * rowHeight);
                this.x = 2600 - (level * colWidth);
            }
            else if (mode == 0) {
                float LEVELRAD = maxNodeSize;

                double radius = ((mem.concepts.levels - level)+1);
                float angle = index; //TEMPORARY
                this.x = (float)(Math.cos(angle/3.0) * radius) * LEVELRAD;
                this.y = (float)(Math.sin(angle/3.0) * radius) * LEVELRAD;
            }
            
            this.name = term;
            this.type = type;
            this.creationTime = creationTime;
        }
    }
    
////Object management - dragging etc.

    class Hsim {

        ArrayList obj = new ArrayList();

        void Init() {
            smooth();
        }

        void mousePressed() {
            if (mouseButton == LEFT) {
                checkSelect();
            }
        }
        boolean dragged = false;

        void mouseDragged() {
            if (mouseButton == LEFT) {
                dragged = true;
                dragElems();
            }
        }

        void mouseReleased() {
            dragged = false;
            selected = null;
        }
        Obj selected = null;

        void dragElems() {
            if (dragged && selected != null) {
                selected.x = hnav.MouseToWorldCoordX(mouseX);
                selected.y = hnav.MouseToWorldCoordY(mouseY);
                hsim_ElemDragged(selected);
            }
        }

        void checkSelect() {
            double selection_distanceSq = selection_distance*selection_distance;
            if (selected == null) {
                for (int i = 0; i < obj.size(); i++) {
                    Obj oi = (Obj) obj.get(i);
                    float dx = oi.x - hnav.MouseToWorldCoordX(mouseX);
                    float dy = oi.y - hnav.MouseToWorldCoordY(mouseY);
                    float distanceSq = (dx * dx + dy * dy);
                    if (distanceSq < (selection_distanceSq)) {
                        selected = oi;
                        hsim_ElemClicked(oi);
                        return;
                    }
                }
            }
        }
    }

//Hamlib handlers
    class Hamlib {

        void Init() {
            noStroke();
            hnav.Init();
            hsim.Init();
        }

        void mousePressed() {
            hnav.mousePressed();
            hsim.mousePressed();
        }

        void mouseDragged() {
            hnav.mouseDragged();
            hsim.mouseDragged();
        }

        void mouseReleased() {
            hnav.mouseReleased();
            hsim.mouseReleased();
        }

        public void mouseMoved() {
        }

        void keyPressed() {
            hnav.keyPressed();
        }

        void mouseScrolled() {
            hnav.mouseScrolled();
        }

        void Camera() {
            hnav.Transform();
        }

        void Update(int r, int g, int b) {
            background(r, g, b);
            pushMatrix();
            Camera();
            hrend_DrawBegin();
            //hsim.Simulate();
            drawit();
            hrend_DrawEnd();
            popMatrix();
        }
    }

}

public class ProcessingGraphPanel extends JFrame {

    applet app = null;
    static boolean had = false; //init already

    public ProcessingGraphPanel(NAR n) {
        super("NARS Graph");
        if (had)
            return;                     
        had = true;
        
        
        
        NARGraph g = new NARGraph();
        g.add(n, IncludeEverything, new NARGraph.DefaultGraphizer(true,true,true,true,true));        

        // create a visualization using JGraph, via an adapter
        JGraphXAdapter jgxAdapter = new JGraphXAdapter(g) {           
            
        };

        
        mxCompactTreeLayout layout2 = 
                new mxCompactTreeLayout(jgxAdapter);                
        layout2.setUseBoundingBox(false);
        layout2.setResizeParent(true);
        layout2.setLevelDistance(30);
        layout2.setNodeDistance(50);
        layout2.execute(jgxAdapter.getDefaultParent());
        
        mxFastOrganicLayout layout = 
                //new mxCompactTreeLayout(jgxAdapter);
                new mxFastOrganicLayout(jgxAdapter);
                //new mxCircleLayout(jgxAdapter);        
        layout.setForceConstant(550);
        layout.execute(jgxAdapter.getDefaultParent());
        
        

        /*
        mxOrganicLayout layout = 
                //new mxCompactTreeLayout(jgxAdapter);
                new mxOrganicLayout(jgxAdapter);
                //new mxCircleLayout(jgxAdapter);        
        layout.setEdgeLengthCostFactor(0.001);*/
        
        
        
        

        
        
        
        

        app = new applet();
        app.graph = g;
        app.layout = jgxAdapter;
        app.init();
        
        this.setSize(1000, 860);//initial size of the window
        this.setVisible(true);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
       
        
        final JCheckBox beliefsEnable = new JCheckBox("Beliefs");
        beliefsEnable.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                app.showBeliefs = (beliefsEnable.isSelected());        
                app.setUpdateNext();
            }
        });
        menu.add(beliefsEnable);
        
        NSlider nodeSize = new NSlider(app.nodeSize, 1, app.maxNodeSize) {
            @Override
            public void onChange(double v) {
                app.nodeSize = (float)v;
            }          
        };
        nodeSize.setPrefix("Node Size: ");
        nodeSize.setPreferredSize(new Dimension(125, 25));
        menu.add(nodeSize);

        content.add(menu, BorderLayout.NORTH);
        content.add(app, BorderLayout.CENTER);
    }
    
    


        
    
    public static void main(String[] args) {
        NAR n = new NAR();
        
        /*
        new TextInput(n, "<a --> b>.");
        new TextInput(n, "<b --> c>.");
        new TextInput(n, "<d <-> c>. %0.75;0.90%");
        new TextInput(n, "<a --> c>?");      
        new TextInput(n, "<a --> d>?");
        n.run(12);
        */
        
        new TextInput(n, "<0 --> num>. %1.00;0.90% {0 : 1}");
        new TextInput(n, "<<$1 --> num> ==> <(*,$1) --> num>>. %1.00;0.90% {0 : 2}"); 
        new TextInput(n, "<(*,(*,(*,0))) --> num>?  {0 : 3}");
       
        n.run(50);
        

        new ProcessingGraphPanel(n);
    }
}
