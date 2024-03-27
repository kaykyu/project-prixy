package vttp.project.app.backend.repository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import vttp.project.app.backend.model.CompletedOrder;

@Repository
public class StatsRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String COLL = "orders";

    public void saveCompletedOrder(CompletedOrder order) {
        mongoTemplate.save(order.toDoc(), COLL);
    }

    public Long today() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime().getTime();
    }

    public MatchOperation matchFilter(Integer days, String id) {
        Long duration = (long) days * 24 * 60 * 60 * 1000;
        return Aggregation.match(Criteria
                .where("ordered_date").gte(new Date(today() - duration))
                .and("client_id").is(id));
    }

    public Document totalSales(MatchOperation matchOps) {
        GroupOperation groupOps = Aggregation.group().sum("amount").as("sales");
        Aggregation pipeline = Aggregation.newAggregation(matchOps, groupOps);
        return mongoTemplate.aggregate(pipeline, COLL, Document.class).getUniqueMappedResult();
    }

    public List<Document> topSelling(MatchOperation matchOps) {
        UnwindOperation unwindOps = Aggregation.unwind("orders");
        GroupOperation groupOps = Aggregation.group("orders.id")
                .sum("orders.quantity").as("quantity")
                .last("orders.name").as("name");
        SortOperation sortOps = Aggregation.sort(Sort.by(Direction.DESC, "quantity"));
        LimitOperation limitOps = Aggregation.limit(3);
        Aggregation pipeline = Aggregation.newAggregation(matchOps, unwindOps, groupOps, sortOps, limitOps);
        return mongoTemplate.aggregate(pipeline, COLL, Document.class).getMappedResults();
    }

    public List<Document> countByHour(MatchOperation matchOps) {
        ProjectionOperation projectOps = Aggregation.project()
                .and(DateOperators.Hour.hourOf("ordered_date")).as("hour")
                .andExclude("_id");
        GroupOperation groupOps = Aggregation.group("hour")
                .count().as("count");
        SortOperation sortOps = Aggregation.sort(Sort.by(Direction.ASC, "_id"));
        Aggregation pipeline = Aggregation.newAggregation(matchOps, projectOps, groupOps, sortOps);
        return mongoTemplate.aggregate(pipeline, COLL, Document.class).getMappedResults();
    }

    public Document getAll(String id, Integer days) throws Exception {
        MatchOperation matchOps = matchFilter(days, id);
        return totalSales(matchOps)
                .append("top", topSelling(matchOps))
                .append("hourly", countByHour(matchOps));
    }
}
