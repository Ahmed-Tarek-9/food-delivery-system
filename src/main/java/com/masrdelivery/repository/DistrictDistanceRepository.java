package com.masrdelivery.repository;

import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.common.DistrictPair;
import com.masrdelivery.exception.DistanceNotFoundException;
import com.masrdelivery.exception.NullEntityException;

import java.util.HashMap;
import java.util.Map;

public class DistrictDistanceRepository {

    private final Map<DistrictPair, Integer> distances;

    public DistrictDistanceRepository() {
        distances = new HashMap<>();
        initializeDistances();
    }

    private void initializeDistances() {

        distances.put(
                new DistrictPair(
                        District.MAADI,
                        District.DOKKI
                ),
                8);

        distances.put(
                new DistrictPair(
                        District.MAADI,
                        District.FAISAL
                ),
                12);

        distances.put(
                new DistrictPair(
                        District.MAADI,
                        District.NASR_CITY
                ),
                16);

        distances.put(
                new DistrictPair(
                        District.MAADI,
                        District.HELIOPOLIS
                ),
                20);

        distances.put(
                new DistrictPair(
                        District.MAADI,
                        District.OCTOBER
                ),
                28);

        distances.put(
                new DistrictPair(
                        District.MAADI,
                        District.REHAB
                ),
                34);

        distances.put(
                new DistrictPair(
                        District.DOKKI,
                        District.FAISAL
                ),
                6);

        distances.put(
                new DistrictPair(
                        District.DOKKI,
                        District.NASR_CITY
                ),
                12);

        distances.put(
                new DistrictPair(
                        District.DOKKI,
                        District.HELIOPOLIS
                ),
                15);

        distances.put(
                new DistrictPair(
                        District.DOKKI,
                        District.OCTOBER
                ),
                22);

        distances.put(
                new DistrictPair(
                        District.DOKKI,
                        District.REHAB
                ),
                28);

        distances.put(
                new DistrictPair(
                        District.FAISAL,
                        District.NASR_CITY
                ),
                18);

        distances.put(
                new DistrictPair(
                        District.FAISAL,
                        District.HELIOPOLIS
                ),
                12);

        distances.put(
                new DistrictPair(
                        District.FAISAL,
                        District.OCTOBER
                ),
                17);

        distances.put(
                new DistrictPair(
                        District.FAISAL,
                        District.REHAB
                ),
                33);

        distances.put(
                new DistrictPair(
                        District.NASR_CITY,
                        District.HELIOPOLIS
                ),
                6);

        distances.put(
                new DistrictPair(
                        District.NASR_CITY,
                        District.OCTOBER
                ),
                26);

        distances.put(
                new DistrictPair(
                        District.NASR_CITY,
                        District.REHAB
                ),
                18);

        distances.put(
                new DistrictPair(
                        District.HELIOPOLIS,
                        District.OCTOBER
                ),
                30);

        distances.put(
                new DistrictPair(
                        District.HELIOPOLIS,
                        District.REHAB
                ),
                16);

        distances.put(
                new DistrictPair(
                        District.OCTOBER,
                        District.REHAB
                ),
                42);
    }

    public int getDistance(District district1, District district2) {
        if (district1 == null || district2 == null) {
            throw new NullEntityException("District");
        }

        if (district1 == district2) {
            return 0;
        }

        Integer distance = distances.get(
                new DistrictPair(
                        district1,
                        district2
                )
        );

        if (distance == null) {
            throw new DistanceNotFoundException("Distance between "
                    + district1
                    + " and "
                    + district2
                    + " does not exist."
            );
        }

        return distance;
    }
}
