package org.jaqpot.core.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Created by Angelos Valsamis on 19/12/2016.
 */
@Mapper
public interface DataEntryMapper {

    DataEntryMapper INSTANCE = Mappers.getMapper( DataEntryMapper.class );

    org.jaqpot.core.model.dto.dataset.Substance substanceToSubstance(org.jaqpot.ambitclient.model.dataset.Substance substance);
}
