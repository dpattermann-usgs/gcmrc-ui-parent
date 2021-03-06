package gov.usgs.cida.gcmrcservices.mb.dao;

import gov.usgs.cida.gcmrcservices.mb.MyBatisConnectionFactory;
import gov.usgs.cida.gcmrcservices.mb.model.DurationCurve;
import gov.usgs.cida.gcmrcservices.mb.model.DurationCurvePoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dmsibley, zmoore
 */
public class DurationCurveDAO {
	private static final Logger log = LoggerFactory.getLogger(DurationCurveDAO.class);
	
	private final SqlSessionFactory sqlSessionFactory;

	public DurationCurveDAO() {
		this.sqlSessionFactory = MyBatisConnectionFactory.getSqlSessionFactory();
	}

	public DurationCurveDAO(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}
	
	public static final String queryPackage = "gov.usgs.cida.gcmrcservices.mb.mappers";
	
	public DurationCurve getDurationCurve(String siteName, String startTime, String endTime, int groupId, int binCount, String binType) {		
		DurationCurve result;
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("siteName", siteName);
		params.put("startTime", startTime);
		params.put("endTime", endTime);
		params.put("binCount", binCount);
		params.put("groupId", groupId);
		params.put("binType", binType);
		
		try (SqlSession session = sqlSessionFactory.openSession()) {
			List<DurationCurvePoint> returnedPoints = session.selectList( queryPackage + ".DurationCurveMapper.getDurationCurve", params);

			//Verify returned points are valid
			boolean invalid = returnedPoints.size() != binCount;
			if(!invalid){
				for(DurationCurvePoint point : returnedPoints){
					if(point.getCumulativeBinPerc() > 100 || point.getCumulativeBinPerc() < 0){
						invalid = true;
						break;
					}
				}
			}
			
			if(!invalid){
				result = new DurationCurve(returnedPoints, siteName, groupId, binType);
			} else {
				log.error("Duration curve query returned invalid data with parameters: [siteName: " + siteName + ", groupId: " + groupId + ", binType: " + binType + "]");
				result = new DurationCurve(null, siteName, groupId, binType);
			}
		} catch (Exception e) {
			log.error("Could not get duration curve with parameters: [siteName: " + siteName + ", groupId: " + groupId + ", binType: " + binType + "] Error: " + e.getMessage());
			result = new DurationCurve(null, siteName, groupId, binType);
		}
				
		return result;
	}
}
