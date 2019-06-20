const getOutliers = (arr) => {
  const medianIndex = getMedian(arr).index;
  const lowerHalf = arr.slice(0, medianIndex);
  const q1 = getMedian(lowerHalf).value;
  const upperHalf = arr.slice(medianIndex);
  const q3 = getMedian(upperHalf).value;
  const qr = q3 - q1;
  const upperBound = q3 + (1.5 * qr);
  const lowerBound = q1 - (1.5 * qr);

  return {
    upper: arr.filter(item => item > upperBound),
    lower: arr.filter(item => item < lowerBound),
  };
};

const getMedian = (arr) => {
  arr.sort((a, b) => (a > b ? 1 : -1));
  const lowMiddleIndex = Math.floor((arr.length - 1) / 2);
  const highMiddleIndex = Math.ceil((arr.length - 1) / 2);
  return {
    value: (arr[lowMiddleIndex] + arr[highMiddleIndex]) / 2,
    index: lowMiddleIndex,
  };
};


export default {
  getOutliers,
  getMedian,
};
