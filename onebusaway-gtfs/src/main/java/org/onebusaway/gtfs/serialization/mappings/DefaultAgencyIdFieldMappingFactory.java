package org.onebusaway.gtfs.serialization.mappings;

import java.util.Map;

import org.onebusaway.gtfs.csv.CsvEntityContext;
import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.csv.schema.DefaultFieldMapping;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.FieldMapping;
import org.onebusaway.gtfs.csv.schema.FieldMappingFactory;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;

/**
 * A {@link FieldMappingFactory} implementation that produces a
 * {@link FieldMapping} that is responsible for setting the
 * {@link AgencyAndId#setAgencyId(String)} portion of an {@link AgencyAndId}
 * identifier.
 * 
 * The GTFS library makes use of {@link AgencyAndId} identifier for most ids for
 * GTFS entities, so as to provide as simple namespace mechanism for loading
 * multiple feeds from different agencies into the same data-store. Since agency
 * ids only appear in a few places in a GTFS feed, if at all, we need some
 * mechanism for setting the agencyId portion of ids for all appropriate
 * entities in the system.
 * 
 * This {@link FieldMappingFactory} and the {@link FieldMapping} it produces
 * does the heavy lifting of setting those agencyId values in an appropriate
 * way.
 * 
 * By default, we use the agencyId returned by
 * {@link GtfsReaderContext#getDefaultAgencyId()}. However, if you specify a
 * property path expression to the
 * {@link #DefaultAgencyIdFieldMappingFactory(String)} constructor, we will
 * evaluate that property path expression against the target entity instance to
 * determine the agencyId. So, for example, to set the agencyId for
 * {@link Route#getId()}, we specify a path of "agency.id", which will call
 * {@link Route#getAgency()} and then {@link Agency#getId()} to set the agency
 * id. See also the path "route.agency.id" for {@link Trip}.
 * 
 * @author bdferris
 * @see GtfsEntitySchemaFactory
 */
public class DefaultAgencyIdFieldMappingFactory implements FieldMappingFactory {

  private String _agencyIdPath = null;

  public DefaultAgencyIdFieldMappingFactory() {
    this(null);
  }

  public DefaultAgencyIdFieldMappingFactory(String agencyIdPath) {
    _agencyIdPath = agencyIdPath;
  }

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      Class<?> entityType, String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required) {

    return new FieldMappingImpl(entityType, csvFieldName, objFieldName,
        String.class, required);
  }

  private class FieldMappingImpl extends DefaultFieldMapping {

    public FieldMappingImpl(Class<?> entityType, String csvFieldName,
        String objFieldName, Class<?> objFieldType, boolean required) {
      super(entityType, csvFieldName, objFieldName, objFieldType, required);
    }

    @Override
    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {
      
      if (isMissingAndOptional(object))
        return;
      
      AgencyAndId id = (AgencyAndId) object.getPropertyValue(_objFieldName);
      csvValues.put(_csvFieldName, id.getId());
    }

    @Override
    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      if (isMissingAndOptional(csvValues))
        return;

      String agencyId = resolveAgencyId(context, object);

      String id = (String) csvValues.get(_csvFieldName);
      AgencyAndId agencyAndId = new AgencyAndId(agencyId, id);
      object.setPropertyValue(_objFieldName, agencyAndId);
    }

    private String resolveAgencyId(CsvEntityContext context, BeanWrapper object) {

      if (_agencyIdPath == null) {
        GtfsReaderContext ctx = (GtfsReaderContext) context.get(GtfsReader.KEY_CONTEXT);
        return ctx.getDefaultAgencyId();
      }

      for (String property : _agencyIdPath.split("\\.")) {
        Object value = object.getPropertyValue(property);
        object = BeanWrapperFactory.wrap(value);
      }

      return object.getWrappedInstance(Object.class).toString();
    }
  }
}
