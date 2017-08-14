import scalax.collection.Graph
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._
import org.apache.poi.ss.usermodel.{WorkbookFactory, DataFormatter}
import org.apache.poi.ss.usermodel.{Cell, CellType}
import org.apache.poi.ss.usermodel.DataFormat
import java.io.{File, FileOutputStream}

object GraphPratice{

  def main(args:Array[String]) {

    import scala.collection.JavaConversions._

    /**
     * The Task class represents the task operations contain in the input file.
     */
    case class Task(

      name:String, 
      startdate: Option[String],

      bcdurext:Double,
      bcdurbpp: Double,

      bconeoffcostext: Option[Double],
      bcdayratext: Option[Double],
      bconeoffcostbpp: Otion[Double],
      bcdayratebpp: Option[Double],

      pdffuncdur: String,
      pdfdurargs: Array[Double],
      pdffunccost: String,
      pdfcostargs: Array[Double]

    )
    
    val regexop = raw"([a-zA-Z]+\d+(\-\d+)?)".r
    val regexpre = raw"[a-zA-Z]+\d+(\-\d+)?".r
    
    val workbook = WorkbookFactory.create(new File("Scenario_Operations_Overview_DEV.xlsx"))
    val sheet = workbook.getSheetAt(0)
    var row = 6 //This is hardcoded Bad!! need some times to improve it.

    /**
     * create root task instance
     */
    val root = Task(
      name = "root", 
      startdate=None, 
      bcdurext = 0, 
      bcdurbpp = 0,
      bconeoffcostext = None,
      bcdayratext = None,
      bconeoffcostbpp = None,
      bcdayratebpp = None,
      pdffuncdur = "",
      pdfdurargs = Array[String](),
      pdffunccost = "",
      pdfcostargs = Array[String]()
    )

    /**
     * This map contains the column reference of each items contain in the input file.
     * Unfortunately, the autodetection of items in the input file is not yet implemented, the following column numbers are hardcoded in the following map.
     */
    val columnrefitems = Map(
      "<op>" -> 13,
      "<pre_op>" -> 14,
      "<start_date>" -> 15,
      "<day_c>" -> 16,
      "<day_b>" -> 17,
      "<one_off_c>" -> 18,
      "<day_rate_c>" -> 19,
      "<one_off_b>" -> 20,
      "<day_rate_b>" -> 21,
      "<pdf_type_duration>" -> 30,
      "<pdf_parameters_durations>" -> 31,
      "<pdf_type_cost>" -> 32,
      "<pdf_parameters_cost>" -> 33
      )

    var opmap:Map[String,Task] = Map("root" -> root)
    var g = Graph[Task,DiEdge]()
    var endfile = false

    while (row < sheet.getPhysicalNumberOfRows() & endfile!=true){

      var currentrow = sheet.getRow(row)

      if (currentrow !=null){

        var name: String =""
        var predecessor = List[String]() 
        var startdate: String = ""
        var bcdurext:Double = 0
        var bcdurbpp: Double = 0
        var bconeoffcostext: Option[Double] = None()
        var bcdayratext: Option[Double] = None()
        var bconeoffcostbpp: Otion[Double] = None()
        var bcdayratebpp: Option[Double] = None()
        var pdffuncdur: String = ""
        var pdfdurargs = Array[Double]()
        var pdffunccost: String = ""
        var pdfcostargs = Array[Double]()

        val cname = currentrow.getCell(columnrefitems("<op>"))
        val cpredecessor = currentrow.getCell(columnrefitems("<pre_op>"))
        val cstartdate = currentrow.getCell(columnrefitems("<start_date>"))
        val cbcdurext = currentrow.getCell(columnrefitems("<day_c>"))
        val cbcdurbpp = currentrow.getCell(columnrefitems("<day_b>"))
        val cbconeoffcostext = currentrow.getCell(columnrefitems("<one_off_c>"))
        val cbcdayratext = currentrow.getCell(columnrefitems("<day_rate_c>"))
        val cbconeoffcostbpp = currentrow.getCell(columnrefitems("<one_off_b>"))
        val cbcdayratebpp = currentrow.getCell(columnrefitems("<day_rate_b>"))
        val cpdffuncdur = currentrow.getCell(columnrefitems("<pdf_type_duration>"))
        val cpdfdurargs = currentrow.getCell(columnrefitems("<pdf_parameters_durations>"))
        val cpdffunccost = currentrow.getCell(columnrefitems("<pdf_type_cost>"))
        val cpdfcostargs = currentrow.getCell(columnrefitems("<pdf_parameters_cost>"))

        if(cname!=null){
          if(cname.getCellTypeEnum()==CellType.STRING){
            name = cname.getStringCellValue() 
              if (name =="<END>") endfile = true
              else name = regexop.findFirstIn(name).getOrElse("")
          }
        }

        if(cpredecessor!=null){
          if(cpredecessor.getCellTypeEnum()==CellType.STRING){
            var predecessors = cpredecessor.getStringCellValue()
            if (predecessors == "<END>") endfile =true
            else{
              predecessor = regexpre.findAllIn(predecessors).toList 
              println("predecessor:"+predecessor)
            }
          }
        }

        if(cbcdurext != null){
          if(cbcdurext.getCellTypeEnum()==CellType.NUMERIC){
            bcdurext = ccvalue.getNumericCellValue()
          }
          else if (cvalue.getCellTypeEnum() == CellType.STRING & cvalue.getStringCellValue() == "<END>") endfile = true
        }

        if (endfile==false & name!=""){ 

          var newTask = Task(name, value)
          opmap+=(name -> newTask)

          if (predecessor.isEmpty()){
            g = g + (opmap("root") ~> newTask)
          }
          else{ 
            for (pred <- predecessor){
              g = g + (opmap(pred) ~> newTask)
            }
          }
        }
        row = row + 1
      }
    }
    println("graph:"+(g mkString "-" ))
  }
}
