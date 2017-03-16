package be.occam.velo.domain.service;

import static be.occam.utils.javax.Utils.*;
import static be.occam.utils.javax.Utils.map;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import be.occam.utils.spring.web.Result;
import be.occam.utils.spring.web.Result.Value;
import be.occam.utils.timing.Timing;
import be.occam.velo.RideDTO;
import be.occam.velo.application.util.DataGuard;
import be.occam.velo.domain.object.Ride;
import be.occam.velo.domain.people.RideManager;

public class RideService {
	
	protected final Logger logger
		= LoggerFactory.getLogger( this.getClass() );
	
	@Resource
	DataGuard dataGuard;
	
	@Resource
	RideManager rideManager;
	
	public RideService( ) {
		logger.info( "ride service started" );
	}
	
	@Transactional( readOnly=true )
	public Result<List<Result<RideDTO>>> query( Boolean today ) {
		
		logger.info( "query, today is [{}]", today );
		
		Result<List<Result<RideDTO>>> result
			= new Result<List<Result<RideDTO>>>();
		
		List<Result<RideDTO>> individualResults
			= list();
		
		List<Ride> rides
			= list( );
		
		List<Ride> all 
			= this.rideManager.all();
		
		if ( today == null ) {
			
			rides.addAll( all );
			
		}
		else {
			
			GregorianCalendar todayCal
				= new GregorianCalendar();
			
			for ( Ride ride : all ) {
				
				GregorianCalendar rideCal
					= new GregorianCalendar();
				rideCal.setTime( ride.getMoment() );
				
				if ( rideCal.get( Calendar.YEAR ) == todayCal.get( Calendar.YEAR ) ) {
					
					if ( rideCal.get( Calendar.DAY_OF_YEAR ) == todayCal.get( Calendar.YEAR ) ) {
						
						rides.add( ride );
						
					}
					
				}
				
			}
			
			
		}
		
		for ( Ride ride: rides ) {
			
			Result<RideDTO> individualResult
				= new Result<RideDTO>();
			
			individualResult.setValue( Value.OK );
			individualResult.setObject( Ride.dto( ride ) );
			
			individualResults.add( individualResult );
			
		}
			
		result.setValue( Value.OK );
		result.setObject( individualResults );
		
		return result;
			
	}
	
	@Transactional( readOnly=false )
	public Result<List<RideDTO>> consume( List<RideDTO> rides ) {
		
		logger.info( "[{}]; consume");
		
		Date now
			= new Date();
		
		Result<List<RideDTO>> result
			= new Result<List<RideDTO>>();
		
		List<RideDTO> consumed
			= list();
		
		result.setValue( Value.OK );
		
		for ( RideDTO ride : rides ) {
			
			Ride l
				= Ride.from( ride );
			
			l.setMoment( now );
			
			if ( isEmpty( l.getTitle() ) ) {
				
				StringBuilder b
					= new StringBuilder();
				
				b.append( "Ride ");
				b.append( Timing.moment( now ) );
				
				l.setTitle( b.toString() );
			}
			
			Ride created 
				= this.rideManager.create( l );
			
			consumed.add( Ride.dto( created ) );
			
		}
		
		result.setObject( consumed );
		
		return result;
		
	}
	
	public RideService guard() {
		this.dataGuard.guard();
		return this;
	}

}
