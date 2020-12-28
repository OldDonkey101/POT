package hx.mbt.pot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import hx.mbt.fsm.*;
import hx.mbt.pot.experiment.TestExp;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class FileUtil {

    public static MealyMachine buildMMachineFromSheet(Sheet sheet){
        // 获得行数
        int rows = sheet.getRows();
        // 获得列数
        int cols = sheet.getColumns();
        // 读取数据
        //Create Input Set
        Set<Input> inputSet = new HashSet<>();
        //Create State List, later to set.
        ArrayList<State> stateList = new ArrayList<>();
        for (int row = 1; row < rows; row++) {
            String stateSignature = sheet.getCell(0, row).getContents();
            stateList.add(new State(stateSignature));
        }
        //Create Output Set
        Set<Output> outputSet = new HashSet<>();

        //Create transitions and add outputs
        Set<Transition> transSet = new HashSet<>();
        for (int col = 1; col < cols; col++)
        {
            //create input
            Input input = new Input(sheet.getCell(col, 0).getContents());
            inputSet.add(input);
            for (int row = 1; row < rows; row++)
            {
                //srcState is statelist.get(row-1);
                String content = sheet.getCell(col, row).getContents();
                String tmp[] = content.split("/");
                String sigState = tmp[0];
                String sigOutput = tmp[1];
                outputSet.add(new Output(sigOutput));
                //Find desState and output
                State desState = null;
                for (State s : stateList) {
                    if (s.getSignature().equals(sigState)) {
                        desState = s;
                        break;
                    }
                }
                Output output = null;
                for (Output o : outputSet) {
                    if (o.getSignature().equals(sigOutput)) {
                        output = o;
                        break;
                    }
                }
                //create transition
                DTransition dTrans = new DTransition(stateList.get(row - 1), input, output, desState);
                transSet.add(dTrans);
            }
        }
        //create machine
        return new MealyMachine(inputSet, outputSet, new HashSet<>(stateList),stateList.get(0),transSet);
    }

    public static MealyMachine buildMMachineFromExcel(String filePath) throws IOException, BiffException {
        File xlsFile = new File(filePath);
        // 获得工作簿对象
        Workbook workbook = Workbook.getWorkbook(xlsFile);
        // 获得所有工作表
        Sheet[] sheets = workbook.getSheets();
        // 遍历工作表
        for (Sheet sheet : sheets) {
            return buildMMachineFromSheet(sheet);
        }
        return null;
    }
    public static Collection<MealyMachine> buildMMachinesFromExcel(String filePath) throws IOException, BiffException {
        File xlsFile = new File(filePath);
        // 获得工作簿对象
        Workbook workbook = Workbook.getWorkbook(xlsFile);
        // 获得所有工作表
        Sheet[] sheets = workbook.getSheets();
        // 遍历工作表
        Collection<MealyMachine> machines = new ArrayList<>();
        for (Sheet sheet : sheets) {
            machines.add(buildMMachineFromSheet(sheet));
        }
        return machines;
    }

    public static FSM buildFSMFromExcel(String filePath) throws IOException, BiffException {
        File xlsFile = new File(filePath);
        // 获得工作簿对象
        Workbook workbook = Workbook.getWorkbook(xlsFile);
        // 获得所有工作表
        Sheet[] sheets = workbook.getSheets();
        // 遍历工作表
        for (Sheet sheet : sheets) {
            return buildFSMFromSheet(sheet);
        }
        return null;
    }
    public static Collection<FSM> buildFSMsFromExcel(String filePath) throws IOException, BiffException {
        File xlsFile = new File(filePath);
        // 获得工作簿对象
        Workbook workbook = Workbook.getWorkbook(xlsFile);
        // 获得所有工作表
        Sheet[] sheets = workbook.getSheets();
        // 遍历工作表
        ArrayList<FSM> fsms = new ArrayList<>();
        for (Sheet sheet : sheets) {
            fsms.add(buildFSMFromSheet(sheet));
        }
        return fsms;
    }

    public static FSM buildFSMFromSheet(Sheet sheet) {
        // 获得行数
        int rows = sheet.getRows();
        // 获得列数
        int cols = sheet.getColumns();
        // 读取数据
        //Create Input Set
        Set<Input> inputSet = new HashSet<>();
        //Create State List, later to set.
        ArrayList<State> stateList = new ArrayList<>();
        for (int row = 1; row < rows; row++) {
            if (sheet.getCell(0, row).getContents().equals("")) {
                rows = row;
                break;
            }
            String stateSignature = sheet.getCell(0, row).getContents();
            stateList.add(new State(stateSignature));
        }
        //Create Output Set
        Set<Output> outputSet = new HashSet<>();

        //Create transitions and add outputs
        Set<Transition> transSet = new HashSet<>();
        for (int col = 1; col < cols; col++) {
            //create input
            if (sheet.getCell(col, 0).getContents().equals("")) {
                cols = col;
                break;
            }
            Input input = new Input(sheet.getCell(col, 0).getContents());
            inputSet.add(input);
            for (int row = 1; row < rows; row++) {
                //srcState is statelist.get(row-1);
                String content = sheet.getCell(col, row).getContents();

                String[] transTexts = content.split(";");
                for (String transText : transTexts) {
                    String tmp[] = transText.split("/");

                    //Find desStates and outputs
                    String[] sigStates = tmp[0].split(",");
                    Set<State> desStateSet = new HashSet<>();
                    for (String sigState : sigStates) {
                        for (State s : stateList) {
                            if (s.getSignature().equals(sigState)) {
                                desStateSet.add(s);
                                break;
                            }
                        }
                    }
                    String sigsOutput = tmp[1];
                    String[] outputSigSet = sigsOutput.split(",");
                    for (String sigOutput : outputSigSet) {
                        outputSet.add(new Output(sigOutput));
                    }
                    Set<Output> outputs = new HashSet<>();
                    for (String sigOutput : outputSigSet) {
                        for (Output o : outputSet) {
                            if (o.getSignature().equals(sigOutput)) {
                                outputs.add(o);
                                break;
                            }
                        }
                    }

                    //create transition
                    Transition transition = new Transition(stateList.get(row - 1),
                            new HashSet<Input>() {{
                                add(input);
                            }},
                            outputs,
                            desStateSet);
                    transSet.add(transition);

                }
            }
        }
        return new FSM(inputSet, outputSet, new HashSet<>(stateList), stateList.get(0), transSet);
    }

    public static HomoMapping buildMappingFromExcel(String filePath) throws IOException, BiffException {
        File xlsFile = new File(filePath);
        // 获得工作簿对象
        Workbook workbook = Workbook.getWorkbook(xlsFile);
        // 获得所有工作表
        Sheet[] sheets = workbook.getSheets();
        // 遍历工作表

        FSM property = null;
        MealyMachine spec = null;
        HomoMapping mapping = null;
        if (sheets != null)
        {
            //先建立 spec 和 property 模型
            for (Sheet sheet : sheets)
            {
                if (sheet.getName().equals("A")) {
                    property = buildFSMFromSheet(sheet);
                } else if (sheet.getName().equals("S")) {
                    spec = buildMMachineFromSheet(sheet);
                }
            }
            //建立mapping
            mapping = new HomoMapping(spec, property);
            for (Sheet sheet : sheets) {
                if (sheet.getName().equals("H")) {
                    for (int row = 1; row < sheet.getRows(); row++) {
                        String sigS = sheet.getCell(0, row).getContents();
                        String sigA = sheet.getCell(1, row).getContents();
                        //Find s and a
                        State s = null;
                        State a = null;
                        for (State state : spec.getStateSet()) {
                            if (state.getSignature().equals(sigS)) {
                                s = state;
                                break;
                            }
                        }
                        for (State state : property.getStateSet()) {
                            if (state.getSignature().equals(sigA)) {
                                a = state;
                                break;
                            }
                        }
                        mapping.addMapping(s, a);
                    }
                }
            }
        }

        workbook.close();

        return mapping;
    }

    public static boolean writeTestExp2Excel(TestExp exp, String filePath, String sheetName) throws IOException, WriteException {
        if (filePath == null) {
            filePath = "D:\\testRecords.xls";
        }
        File file = new File(filePath);

        // 创建用于写入内容的Excel文件的引用
        WritableWorkbook wb=null;
        Workbook wbRead = null;
        try {
            // 获取Excel文件
            if (!file.exists()) {
                file.createNewFile();
                wb = Workbook.createWorkbook(file);
            }else{
                wbRead = Workbook.getWorkbook(file);
                wb=Workbook.createWorkbook(file, wbRead);
            }
            if (wb != null) {
                WritableSheet sheet = null;
                //find the sheet with given name.
                for (WritableSheet eachSheet: wb.getSheets()) {
                    if (eachSheet.getName().equals(sheetName)) {
                        sheet = eachSheet;
                        break;
                    }
                }
                if(sheet==null) sheet = wb.createSheet(sheetName,0);
                int cursor = sheet.getRows();
                //Check if the excel is new.
                if (cursor == 0 || sheet.getCell(0, 0).getContents().equals("")) {
                    // New excel,add title.
                    ArrayList<String> titles = new ArrayList<>();
                    //test args
                    titles.add("number of inputs");
                    titles.add("number of outputs");
                    titles.add("number of extra states");

                    //test result
                    titles.add("size of spec");
                    titles.add("size of prop");
                    titles.add("transNum of spec");
                    titles.add("transNum of prop");
                    titles.add("size of TSP");
                    titles.add("size of TSH");
                    titles.add("Steps of TSP");
                    titles.add("Steps of TSH");

                    for (int i = 0; i < titles.size(); i++) {
                        sheet.addCell(new Label(i, cursor, titles.get(i)));
                    }
                    cursor = 1;
                }
                if (exp.isExecuted()) {
                    ArrayList<String> datas = new ArrayList<>();
                    datas.add(exp.getMapping().getS().getInputSet().size() + "");
                    datas.add(exp.getMapping().getS().getOutputSet().size() + "");
                    datas.add(exp.getK() + "");
                    datas.add(exp.getSpecN()+"");
                    datas.add(exp.getPropN()+"");
                    datas.add(exp.getSpecTranNum()+"");
                    datas.add(exp.getPropTransNum()+"");
                    datas.add(exp.getTSizePOT() +"");
                    datas.add(exp.getTSizeConf() + "");
                    datas.add(exp.getTStepPOT() + "");
                    datas.add(exp.getTStepConf() +"");
                    for (int i = 0; i < datas.size(); i++) {
                        sheet.addCell(new Label(i, cursor, datas.get(i)));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            wb.write();
            wb.close();
        }
      return true;
    }

    public static void main(String[] args) {
        FSM machine = null;
        try {
            machine = buildMMachineFromExcel("C:\\Users\\87720\\OneDrive\\Coding Workspace\\IdeaProjects\\TestPlatform\\assets\\A.xls");
            System.out.println(machine.print());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }
}

