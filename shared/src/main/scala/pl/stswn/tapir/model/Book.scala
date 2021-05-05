package pl.stswn.tapir.model

case class Book(
                 id: Long,
                 title: String,
                 author: Option[String],
                 country: Option[String],
                 year: Option[Int],
                 genre: Option[String]
               )

object Book {
  val sampleBooks = List(
    Book(0, "The Da Vinci Code", Some("Dan Brown"), Some("US"), Some(2004), Some("Crime")),
    Book(1, "Harry Potter and the Philosopher's Stone", Some("J.K. Rowling"), Some("GB"), Some(1997), Some("Fiction")),
    Book(2, "Fifty Shades Of Grey", Some("E.L. James"), Some("GB"), Some(2011), Some("Romance")),
    Book(3, "Twilight", Some("Stephanie Meyer"), Some("US"), Some(2005), Some("Fiction")),
    Book(4, "Girl with the Dragon Tattoo", Some("Stieg Larsson"), Some("SE"), Some(2005), Some("Crime")),
    Book(5, "The Lovely Bones", Some("Alice Sebold"), Some("US"), Some(2002), Some("Novel")),
    Book(6, "The Curious Incident of the Dog in the Nighttime", Some("Mark Haddon"), Some("GB"), Some(2003), Some("Mystery")),
    Book(7, "A Short History of Nearly Everything", Some("Bill Bryson"), Some("GB"), Some(2003), None),
    Book(8, "The Very Hungry Caterpillar", Some("Eric Carle"), Some("US"), Some(1969), None),
    Book(9, "Jamie's 30-Minute Meals", Some("Jamie Olivier"), Some("GB"), Some(2010), Some("Cookbook")),
  )
}
