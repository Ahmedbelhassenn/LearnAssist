import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth-service/auth.service';

export const instructorGuard: CanActivateFn = (route, state) => {
  const router=inject(Router);
  const service= inject(AuthService);
  const jwtToken = localStorage.getItem('userToken')|| '';
  const role=localStorage.getItem('userRole');
  if(jwtToken!=''){
    if(role==='ROLE_INSTRUCTOR' && !service.isTokenExpired(jwtToken)){
      return true;
    }
    else{
      router.navigateByUrl('/instructor-login');
      return false;
    }
  }
  
  else{
    router.navigateByUrl('/instructor-login');
    return false;
  }
};
