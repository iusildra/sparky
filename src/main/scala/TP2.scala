import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SQLImplicits
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.sql.functions.{udf => _, _}
import scala3encoders.given
import scala3udf.{Udf => udf}
import org.apache.spark.sql.types.IntegerType

val spark = SparkSession.builder
  .master("local[6]")
  .appName("Word Count")
  .getOrCreate()
import spark.implicits.given
import org.apache.spark.ml.feature.Imputer
val sc = spark.sparkContext

val rawDF = spark.read
  .option("header", "true")
  .option("multiLine", "true")
  .option("inferSchema", "true")
  .option("escape", "\"")
  .csv("/home/lucasn/Projects/sparky/datasets/tp2.csv")

object TP2 extends App {
  val baseDF = rawDF.select(
    "host_is_superhost",
    "cancellation_policy",
    "instant_bookable",
    "host_total_listings_count",
    "neighbourhood_cleansed",
    "latitude",
    "longitude",
    "property_type",
    "room_type",
    "accommodates",
    "bathrooms",
    "bedrooms",
    "beds",
    "bed_type",
    "minimum_nights",
    "number_of_reviews",
    "review_scores_rating",
    "review_scores_accuracy",
    "review_scores_cleanliness",
    "review_scores_checkin",
    "review_scores_communication",
    "review_scores_location",
    "review_scores_value",
    "price"
  )

  baseDF.cache().count
  baseDF.describe("bathrooms", "bedrooms", "beds").show()

  val fixedPriceDF = baseDF.withColumn("price", translate($"price", "$,", "").cast("double"))
  val noNullsDF = fixedPriceDF.filter($"host_is_superhost".isNotNull)

  val integerColumns = for (x <- baseDF.schema.fields if (x.dataType == IntegerType)) yield x.name
  val strColumns = Array(
    "bedrooms",
    "bathrooms",
    "beds",
    "review_scores_rating",
    "review_scores_accuracy",
    "review_scores_cleanliness",
    "review_scores_checkin",
    "review_scores_communication",
    "review_scores_location",
    "review_scores_value"
  )
  var doublesDF = noNullsDF

  for (c <- integerColumns ++ strColumns)
    doublesDF = doublesDF.withColumn(c, col(c).cast("double"))

  val imputeCols = Array(
    "bedrooms",
    "bathrooms",
    "beds",
    "review_scores_rating",
    "review_scores_accuracy",
    "review_scores_cleanliness",
    "review_scores_checkin",
    "review_scores_communication",
    "review_scores_location",
    "review_scores_value"
  )

  for (c <- imputeCols)
    doublesDF = doublesDF.withColumn(c + "_na", when(col(c).isNull, 1.0).otherwise(0.0))

  val imputer = new Imputer()
    .setStrategy("median")
    .setInputCols(imputeCols)
    .setOutputCols(imputeCols)

  val imputedDF = imputer.fit(doublesDF).transform(doublesDF)
  val minMax = doublesDF.select(
    min($"price").as("min"),
    max($"price").as("max")
  ).first
  val free = doublesDF.filter("price == 0")
  println(free.count())
  val noFree = doublesDF.filter($"price" > 0)
  println(noFree.count())
}
