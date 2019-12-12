# spark-streamer

Purpose :
Make Spark streamer pickup Customer configuration changes from RDBMS in real time.
Process the event by customer and sink them forward.
 
Frameworks used and dependent :
Spark 2.4.3
Kafka 2+
Any RDBMS 
Maven 3+

Setup steps:
Configure DB configurations in 
src/main/resources/config.properties
AND
pass the above config absolute path as the param to the main class as program arg


Start Program:
com.deepak.data.processor.StreamProcessor 

You can find sample JSON log in below file,
src/main/resources/sample_json.txt
