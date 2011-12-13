package fr.issamax.dao.elastic.factory;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.node.Node;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


/**
 * A {@link FactoryBean} implementation used to create a {@link Client} element
 * from a {@link Node}.
 * <p>
 * The lifecycle of the underlying {@link Client} instance is tied to the
 * lifecycle of the bean via the {@link #destroy()} method which calls
 * {@link Client#close()}
 * 
 * @author David Pilato
 */
public class ElasticsearchClientFactoryBean implements FactoryBean<Client>,
		InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired Node node;

	private Client client;

//	@Override
	public void afterPropertiesSet() throws Exception {
		logger.info("Starting ElasticSearch client");
		if (node == null) throw new Exception("You must define an ElasticSearch Node as a Spring Bean.");
		client = node.client();
		
		initMapping();
	}

//	@Override
	public void destroy() throws Exception {
		try {
			logger.info("Closing ElasticSearch client");
			if (client != null) {
				client.close();
			}
		} catch (final Exception e) {
			logger.error("Error closing Elasticsearch client: ", e);
		}
	}

//	@Override
	public Client getObject() throws Exception {
		return client;
	}

//	@Override
	public Class<Client> getObjectType() {
		return Client.class;
	}

//	@Override
	public boolean isSingleton() {
		return true;
	}

	public void initMapping() throws IOException, InterruptedException {
		try {
			node.client().admin().indices()
					.delete(new DeleteIndexRequest("index")).actionGet();
			// We wait for one second to let ES delete the index
			Thread.sleep(1000);
		} catch (IndexMissingException e) {
			// Index does not exist... Fine
		}

		// Creating the index
		node.client().admin().indices().create(new CreateIndexRequest("index"))
				.actionGet();
		
		XContentBuilder xbMapping = 
			jsonBuilder().startObject()
				.startObject("document")
					.startObject("properties")
						.startObject("name")
							.field("type", "string")
						.endObject()
						.startObject("postDate")
							.field("type", "date")
						.endObject()
						.startObject("file")
							.field("type", "attachment")
							.startObject("fields") 
								.startObject("title")
									.field("store","yes")
								.endObject()
								.startObject("file")
									.field("term_vector","with_positions_offsets")
									.field("store","yes")
								.endObject()
							.endObject()
						.endObject()
					.endObject()
				.endObject()
			.endObject();
		
		System.out.println(xbMapping.string());
		
		node.client().admin().indices().preparePutMapping("index")
				.setType("document").setSource(xbMapping).execute().actionGet();
	}
}