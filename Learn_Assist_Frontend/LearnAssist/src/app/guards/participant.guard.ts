import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth-service/auth.service';

export const participantGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const service=inject(AuthService);
  const role=localStorage.getItem('userRole');
  const jwt=localStorage.getItem('userToken') || '';
  if(jwt!= ''){
    if (role==='ROLE_PARTICIPANT' && !service.isTokenExpired(jwt)) { 
      return true;
    }
    else{
      router.navigateByUrl('/participant-login');
      return false;
    }
  }
  else {
    router.navigateByUrl('/participant-login'); 
    return false;
  }
  
};

