import scalax.collection.Graph
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._
import scalax.collection.GraphTraversal.Successors

import org.apache.poi.ss.usermodel.{WorkbookFactory, DataFormatter}
import org.apache.poi.ss.usermodel.{Cell, CellType}
import org.apache.poi.ss.usermodel.DataFormat

import breeze.stats.distributions.{LogNormal,Gaussian}
import java.io.{File, FileOutputStream}
import scala.collection.mutable.ArrayBuffer

object Model{

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
      bconeoffcostbpp: Option[Double],
      bcdayratebpp: Option[Double],

      pdffuncdur: String,
      pdfdurargs: Array[String],
      pdffunccost: String,
      pdfcostargs: Array[String]

    )
    /* useful regex which analyse the text */
    
    val regexop = raw"([a-zA-Z]+\d+(\-\d+)?)".r
    val regexpre = raw"([a-zA-Z]+\d+(?:\-\d+)?)".r
    val regexpdfarg = raw"(\d+(?:\.\d+)?),(\d+(?:\.\d+)?)".r  

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

    /**
     * Map the pdf ref to real functions
     */
    val pdfrefitems = Map(
      "log_normal" -> "LogNormal",
      "normal" -> "Gaussian"
      )

    var opmap:Map[String,Task] = Map("root" -> root)
    var g = Graph[Task,DiEdge]()
    var endfile = false

    while (row < sheet.getPhysicalNumberOfRows() & endfile!=true){

      var currentrow = sheet.getRow(row)

      if (currentrow !=null) {

        var name: String =""
        var predecessor = List[String]() 
        var startdate: Option[String] = None
        var bcdurext:Double = 0
        var bcdurbpp: Double = 0
        var bconeoffcostext: Option[Double] = None
        var bcdayratext: Option[Double] = None
        var bconeoffcostbpp: Option[Double] = None
        var bcdayratebpp: Option[Double] = None
        var pdffuncdur: String = ""
        var pdfdurargs = Array[String]()
        var pdffunccost: String = ""
        var pdfcostargs = Array[String]()

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

        if(cname != null){
          if(cname.getCellTypeEnum() == CellType.STRING){
            name = cname.getStringCellValue() 
            if (name =="<END>") endfile = true
            else name = regexop.findFirstIn(name).getOrElse("")
          }
        }

        if(cpredecessor != null){
          if(cpredecessor.getCellTypeEnum() == CellType.STRING){
            var predecessors = cpredecessor.getStringCellValue()
            if(predecessors == "<END>") endfile = true
            else predecessor = regexpre.findAllIn(predecessors).toList
          }
        }

        if(cstartdate != null){
          if(cstartdate.getCellTypeEnum() == CellType.STRING){
             cstartdate.getStringCellValue() match {
               case "<END>" => endfile = true
               case default => startdate = Some(default)
            }
          }
        }

        if(cbcdurext != null){
          if(cbcdurext.getCellTypeEnum() == CellType.NUMERIC){
            bcdurext = cbcdurext.getNumericCellValue()
          }
          else if (cbcdurext.getCellTypeEnum() == CellType.STRING & cbcdurext.getStringCellValue() == "<END>") endfile = true
        }

        if(cbcdurbpp != null){
          if(cbcdurbpp.getCellTypeEnum() == CellType.NUMERIC){
            bcdurbpp = cbcdurbpp.getNumericCellValue()
          }
          else if (cbcdurbpp.getCellTypeEnum() == CellType.STRING & cbcdurbpp.getStringCellValue() == "<END>") endfile = true
        }

        if(cbconeoffcostext != null){
          if(cbconeoffcostext.getCellTypeEnum() == CellType.NUMERIC){
            bconeoffcostext = Some(cbconeoffcostext.getNumericCellValue())
          }
          else if (cbconeoffcostext.getCellTypeEnum() == CellType.STRING & cbconeoffcostext.getStringCellValue() == "<END>") endfile = true
        }

        if(cbcdayratext != null){
          if(cbcdayratext.getCellTypeEnum() == CellType.NUMERIC){
            bcdayratext = Some(cbcdayratext.getNumericCellValue())
          }
          else if (cbcdayratext.getCellTypeEnum() == CellType.STRING & cbcdayratext.getStringCellValue() == "<END>") endfile = true
        }

        if(cbconeoffcostbpp != null){
          if(cbconeoffcostbpp.getCellTypeEnum() == CellType.NUMERIC){
            bconeoffcostbpp = Some(cbconeoffcostbpp.getNumericCellValue())
          }
          else if (cbconeoffcostbpp.getCellTypeEnum() == CellType.STRING & cbconeoffcostbpp.getStringCellValue() == "<END>") endfile = true
        }

        if(cbcdayratebpp != null){
          if(cbcdayratebpp.getCellTypeEnum() == CellType.NUMERIC){
            bcdayratebpp = Some(cbcdayratebpp.getNumericCellValue())
          }
          else if (cbcdayratebpp.getCellTypeEnum() == CellType.STRING & cbcdayratebpp.getStringCellValue() == "<END>") endfile = true
        }

        if(cpdffuncdur != null){
          if(cpdffuncdur.getCellTypeEnum() == CellType.STRING){
            cpdffuncdur.getStringCellValue() match {
              case "<END>" => endfile = true
              case "log_normal" => pdffuncdur = pdfrefitems("log_normal")
              case "normal" => pdffuncdur = pdfrefitems("normal")
            }
          }
        }

        if(cpdfdurargs != null){
          if(cpdfdurargs.getCellTypeEnum() == CellType.STRING){
            cpdfdurargs.getStringCellValue() match {
              case "<END>" => endfile = true
              //case regexpdfarg => pdfdurargs = regexpdfarg.findAllIn(cpdfdurargs.getStringCellValue()).toArray
              case regexpdfarg(mean,std) => pdfdurargs = Array(mean,std)
	    }
          }
        }

        if(cpdffunccost != null){
          if(cpdffunccost.getCellTypeEnum() == CellType.STRING){
            cpdffunccost.getStringCellValue() match {
              case "<END>" => endfile = true
              case "log_normal" => pdffunccost = pdfrefitems("log_normal") 
              case "normal" => pdffunccost = pdfrefitems("normal")
            }
          }
        }

        if(cpdfcostargs != null){
          if(cpdfcostargs.getCellTypeEnum()==CellType.STRING){
            cpdfcostargs.getStringCellValue() match {
              case "<END>" => endfile = true
              case regexpdfarg(mean, std) => pdfcostargs = Array(mean, std) 
            }
          }
        }

        if (endfile == false & name != ""){ 

          var newTask = Task(
            name,
            startdate, 
            bcdurext,
            bcdurbpp, 
            bconeoffcostext,
            bcdayratext, 
            bconeoffcostbpp, 
            bcdayratebpp,
            pdffuncdur,
            pdfdurargs, 
            pdffunccost,
            pdfcostargs 
          )
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

    //println("graph:"+(g.nodes mkString "\n" ))
    val innerRoot = g get opmap("root")
    val traverser = innerRoot.outerNodeTraverser.withDirection(Successors)
    println("result: "+traverser.toList)
    val myfunction = (currentNode: Task) => println("bcdurext: "+currentNode.bcdurext)
    traverser.foreach(myfunction)
    //println(("" /: result)(_+_)) // "(A[B1][B2])"    
    //println(g.nodes.head.outerNodeTraverser.withDirection(Successors) mkString "\n")
  }
}

