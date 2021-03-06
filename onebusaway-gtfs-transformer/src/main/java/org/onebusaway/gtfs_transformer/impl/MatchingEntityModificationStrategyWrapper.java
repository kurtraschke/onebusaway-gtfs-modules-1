package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.factory.PropertyMatches;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class MatchingEntityModificationStrategyWrapper extends
    AbstractEntityModificationStrategy {

  private EntityTransformStrategy _strategy;

  public MatchingEntityModificationStrategyWrapper(
      PropertyMatches propertyMatches, EntityTransformStrategy strategy) {
    super(propertyMatches);
    _strategy = strategy;
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao,
      BeanWrapper entity) {
    if (isModificationApplicable(entity))
      _strategy.run(context, dao, entity);
  }

}
