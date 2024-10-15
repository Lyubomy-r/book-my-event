

import java.io.IOException;



//@SpringBootTest
//@TestConfiguration
public class EmbeddedMongoConfig {
//  private Mongod mongodExecutable;
//  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2")
//      .withExposedPorts(27017)
//      .waitingFor(Wait.forListeningPort());
//
//  static {
//    mongoDBContainer.start();
//  }
//
//  // Method to get the MongoDB connection URL with a specified database name
//  private String getMongoUrl(String dbName) {
//    return String.format("mongodb://%s:%d/%s",
//        mongoDBContainer.getContainerIpAddress(),
//        mongoDBContainer.getMappedPort(27017),
//        dbName);
//  }
//
//  @Bean
//  @Primary
//  public MongoTemplate mongoTemplate() {
//    String databaseName = "test"; // Replace with your desired database name
//    String mongoUrl = getMongoUrl(databaseName);
//    System.out.println("Connecting to MongoDB at: " + mongoUrl); // Log the connection URL
//    return new MongoTemplate(new SimpleMongoClientDbFactory(mongoUrl));
//  }
//
//  @Bean
//  @Primary
//  public MongoClient mongoClient() {
//    String mongoUrl = getMongoUrl("test"); // Same database name
//    System.out.println("Creating MongoClient for: " + mongoUrl);
//    return MongoClients.create(mongoUrl);
//  }
//
//  @Bean
//  public GridFsOperations gridFsOperations(MongoTemplate mongoTemplate) {
//    return new GridFsTemplate(mongoTemplate.getMongoDbFactory(), mongoTemplate.getConverter());
//  }
//
//  @AfterAll
//  static void stopContainer() {
//    mongoDBContainer.stop();
//  }
}
