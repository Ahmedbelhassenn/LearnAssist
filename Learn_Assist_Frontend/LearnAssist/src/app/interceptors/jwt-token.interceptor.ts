import { HttpInterceptorFn } from '@angular/common/http';

export const jwtTokenInterceptor: HttpInterceptorFn = (req, next) => {
  const token=localStorage.getItem('userToken');
  const newReq=req.clone({
    setHeaders:{
      Authorization: `Bearer ${token}`
    }
  })
  return next(newReq);
};
